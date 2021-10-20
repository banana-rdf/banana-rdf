package org.w3.banana.jena

import org.apache.jena.datatypes.{BaseDatatype, RDFDatatype, TypeMapper}
import org.apache.jena.graph.{BlankNodeId, GraphUtil, Node_Blank, Node_Literal, Node_URI}
import org.apache.jena.graph.Node.ANY as JenaANY
import org.w3.banana.{Ops, RDF}
import org.w3.banana.operations

import scala.reflect.TypeTest
import scala.util.Try
import scala.util.Using
import scala.util.Using.Releasable
import org.apache.jena.util.iterator.ExtendedIterator

import scala.annotation.targetName

object JenaRdf extends RDF {
	import org.apache.jena.graph as jena
	import org.apache.jena.graph.{NodeFactory, Factory}

	//jena.Graph is modifiable, but we provide no altering methods and always produce new graphs
	//todo? provide jena traits for rNode and rTriple
	override opaque type rGraph = jena.Graph
	override opaque type rTriple = jena.Triple
	// type rNode =  rURI | Node == jena.Node
	override opaque type rURI = jena.Node_URI

	override opaque type Graph = jena.Graph
	override opaque type Triple <: Matchable = jena.Triple
	override opaque type Node <: Matchable = jena.Node
	override opaque type URI <: Node = jena.Node_URI
	override opaque type BNode <: Node = jena.Node_Blank
	override opaque type Literal <: Node = jena.Node_Literal
	override opaque type Lang <: Matchable = String

	override type NodeAny = Null

	given [T]: Releasable[ExtendedIterator[T]] with {
		def release(resource: ExtendedIterator[T]): Unit = resource.close()
	}

	type QuadSubject = Node
	type QuadRelation = URI
	type QuadObject = Node
	type QuadGraph = Graph


	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
	 * I did not succeed in removing the duplication, as there are Match Type compilation problems.
	 * It does not work to place here the implementations of rdf which can be placed above,
	 * as the RDF.Graph[R] type hides the implementation type (of `graph` field for example) **/
	given ops: Ops[R] with {

		val `*`: RDF.NodeAny[R] = null
		given Graph: operations.Graph[R] with
			import RDF.Statement as St
			def empty: RDF.Graph[R] = Factory.empty().nn
			def apply(triples: Iterable[RDF.Triple[R]]): RDF.Graph[R] =
				val graph: Graph = Factory.createDefaultGraph.nn
				triples.foreach { triple =>
					graph.add(triple)
				}
				graph
			//note: how should one pass on the information that the Iterable is closeable?
			// https://stackoverflow.com/questions/69153609/is-there-a-cross-platform-autocloseable-iterable-
			def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] =
				import collection.JavaConverters.asScalaIteratorConverter
				graph.find(JenaANY, JenaANY, JenaANY).nn.asScala.to(Iterable)
			def graphSize(graph: RDF.Graph[R]): Int = graph.size()
			def gunion(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
				val g = Factory.createDefaultGraph.nn
				graphs.foreach { graph =>
					Using.resource(graph.find(JenaANY, JenaANY, JenaANY).nn) { it =>
						while it.hasNext do g.add(it.next)
					}
				}
				g
			def difference(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
				val g = Factory.createDefaultGraph.nn
				GraphUtil.addInto(g, g1)
				GraphUtil.delete(g, g2.find(JenaANY, JenaANY, JenaANY))
				g
			def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
				left.isIsomorphicWith(right)

			def findTriples(graph: RDF.Graph[R],
				s: St.Subject[R]|RDF.NodeAny[R], p: St.Relation[R]|RDF.NodeAny[R], o: St.Object[R]|RDF.NodeAny[R]
			): Iterator[RDF.Triple[R]] =
				import scala.jdk.CollectionConverters.*
				graph.find(s, p, o).nn.asScala
		end Graph

		val rGraph = new rGraphOps:
			def empty: RDF.rGraph[R] = Graph.empty
			def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
				Graph(triples)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				Graph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				Graph.graphSize(graph)

		val rTriple = new rTripleOps:
			import RDF.rStatement as rSt
			def apply(s: rSt.Subject[R], p: rSt.Relation[R], o: rSt.Object[R]): RDF.rTriple[R] =
				jena.Triple.create(s, p, o).nn
			def untuple(t: RDF.Triple[R]): rTripleI =
				(subjectOf(t),relationOf(t),objectOf(t))
			def subjectOf(t: RDF.rTriple[R]): rSt.Subject[R] =
				t.getSubject().asInstanceOf[rSt.Subject[R]].nn
			def relationOf(t: RDF.rTriple[R]): rSt.Relation[R] =
				t.getPredicate().asInstanceOf[URI].nn
			def objectOf(t: RDF.rTriple[R]): rSt.Object[R] =
				t.getObject().asInstanceOf[rSt.Object[R]].nn


		//		given tripleTT: TypeTest[Matchable, RDF.Triple[R]] with {
//			override def unapply(s: Matchable): Option[s.type & Triple] =
//				s match
//					//note: this does not compile if we use URI instead of jena.Node_URI
//					case x: (s.type & Triple) => Some(x)
//					case _ => None
//		}

		given Triple: operations.Triple[R] with {
			import RDF.Statement as St
			def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Triple[R] =
				jena.Triple.create(s, p, o).nn
			def untuple(t: RDF.Triple[R]): TripleI =
					(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.Triple[R]): St.Subject[R] =
				t.getSubject().asInstanceOf[St.Subject[R]].nn
			def relationOf(t: RDF.Triple[R]): St.Relation[R] =
				t.getPredicate.asInstanceOf[St.Relation[R]].nn
			def objectOf(t: RDF.Triple[R]): St.Object[R] =
				t.getObject().asInstanceOf[St.Object[R]].nn
		}

		given Statement: StatementOps with
			extension (subj: RDF.Statement.Subject[R])
				def fold[A](uriFnct: RDF.URI[R] => A, bnFcnt: RDF.BNode[R] => A): A =
					if subj.isBlank then
						bnFcnt(subj.asInstanceOf[Node_Blank])
					else
						uriFnct(subj.asInstanceOf[Node_URI])


		given Node: NodeOps with
			private def jn(node: RDF.Node[R]) = node.asInstanceOf[org.apache.jena.graph.Node]
			extension (node: RDF.Node[R])
				def isURI: Boolean = jn(node).isURI
				def isBNode: Boolean = jn(node).isBlank
				def isLiteral: Boolean = jn(node).isLiteral
		end Node

		given BNode: BNodeOps with
			def apply(label: String): RDF.BNode[R] =
				val id = BlankNodeId.create(label).nn
				NodeFactory.createBlankNode(id).asInstanceOf[Node_Blank]
			def apply(): RDF.BNode[R] =
				NodeFactory.createBlankNode().asInstanceOf[Node_Blank]
			extension (bn: RDF.BNode[R])
				def label: String = bn.getBlankNodeLabel().nn
		end BNode


		given Literal: LiteralOps with
			private val xsdString: RDFDatatype = mapper.getTypeByName(xsdStr).nn
			private val xsdLangString: RDFDatatype = mapper.getTypeByName(xsdLangStr).nn
			//todo? are we missing a Datatype Type? (check other frameworks)

			def jenaDatatype(datatype: URI): RDFDatatype =
				val iriString: String = URI.asString(datatype)
				val typ: RDFDatatype | Null = mapper.getTypeByName(iriString)
				if typ == null then
					val datatype = new BaseDatatype(iriString)
					mapper.registerDatatype(datatype)
					datatype
				else typ

			import LiteralI as Lit

			lazy val mapper: TypeMapper = TypeMapper.getInstance.nn

			override
			def apply(plain: String): RDF.Literal[R] =
				NodeFactory.createLiteral(plain).nn.asInstanceOf[Literal]

			override
			def apply(lit: Lit): RDF.Literal[R] = lit match
				case Lit.Plain(text) => NodeFactory.createLiteral(text).nn.asInstanceOf[Literal]
				case Lit.`@`(text, lang) => Literal(text, lang)
				case Lit.`^^`(text, tp) => Literal(text, tp)

			@targetName("langLit") override
			def apply(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] =
				NodeFactory.createLiteral(lex, lang).nn.asInstanceOf[Literal]

			@targetName("dataTypeLit") override
			def apply(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] =
				NodeFactory.createLiteral(lex, jenaDatatype(dataTp)).nn.asInstanceOf[Literal]

			def unapply(x: Matchable): Option[Lit] =
				x match
					case lit: Literal =>
						val lex: String = lit.getLiteralLexicalForm.nn
						val dt: RDFDatatype | Null = lit.getLiteralDatatype
						val lang: String | Null = lit.getLiteralLanguage
						if (lang == null || lang.isEmpty) then
							if dt == null || dt == xsdString then Some(Lit.Plain(lex))
							else Some(Lit.^^(lex, URI(dt.getURI.nn)))
						else if dt == null || dt == xsdLangString then
							Some(Lit.`@`(lex, Lang(lang)))
						else None
					case _ => None

			extension (lit: RDF.Literal[R])
				def text: String = lit.getLiteralLexicalForm.nn
		end Literal

		given literalTT: TypeTest[Matchable,RDF.Literal[R]] with {
			override def unapply(s: Matchable): Option[s.type & jena.Node_Literal] =
				s match
					//note: this does not compile if we use URI instead of jena.Node_URI
					case x: (s.type & jena.Node_Literal) => Some(x)
					case _ => None
		}

		given Lang:  LangOps with
			def apply(lang: String): RDF.Lang[R] = lang
			extension (lang: RDF.Lang[R])
				def label: String =  lang
		end Lang

		val rURI = new rURIOps:
			def apply(uriStr: String): RDF.rURI[R] =
				NodeFactory.createURI(uriStr).nn.asInstanceOf[URI]
			def asString(uri: RDF.rURI[R]): String =
				uri.getURI().nn

		given URI: URIOps with
			//todo: this never fails to parse. Need to find a way to align behaviors
			def mkUri(iriStr: String): Try[RDF.URI[R]] =
				Try(NodeFactory.createURI(iriStr).asInstanceOf[URI])
			def asString(uri: RDF.URI[R]): String =
				uri.getURI().nn
		end URI

		given uriTT: TypeTest[Node,URI] with {
			override def unapply(s: Node): Option[s.type & jena.Node_URI] =
				s match
					//note: this does not compile if we use URI instead of jena.Node_URI
					case x: (s.type & jena.Node_URI) => Some(x)
					case _ => None
		}

	}
}