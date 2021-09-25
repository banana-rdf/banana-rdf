package org.w3.banana.jena

import org.apache.jena.datatypes.{BaseDatatype, RDFDatatype, TypeMapper}
import org.apache.jena.graph.{BlankNodeId, GraphUtil, Node_Blank, Node_Literal, Node_URI}
import org.apache.jena.graph.Node.ANY as JenaANY
import org.w3.banana.{Ops, RDF}

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

	given [T]: Releasable[ExtendedIterator[T]] with {
		def release(resource: ExtendedIterator[T]): Unit = resource.close()
	}

	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
	 * I did not succeed in removing the duplication, as there are Match Type compilation problems.
	 * It does not work to place here the implementations of rdf which can be placed above,
	 * as the RDF.Graph[R] type hides the implementation type (of `graph` field for example) **/
	given ops: Ops[R] with {

		given Graph: GraphOps with {
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
			def union(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
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
		}

		val rGraph = new rGraphOps:
			def empty: RDF.rGraph[R] = Graph.empty
			def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
				Graph(triples)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				Graph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				Graph.graphSize(graph)

		val rTriple = new rTripleOps:
			def apply(s: RDF.rNode[R], p: RDF.rURI[R], o: RDF.rNode[R]): RDF.rTriple[R] =
				Triple(s, p, o)
			def untuple(t: RDF.Triple[R]): rTripleI =
				(subjectOf(t),relationOf(t),objectOf(t))
			def subjectOf(t: RDF.rTriple[R]): RDF.rNode[R] =
				Triple.subjectOf(t)
			def relationOf(t: RDF.rTriple[R]): RDF.rURI[R] =
				Triple.relationOf(t)
			def objectOf(t: RDF.rTriple[R]): RDF.rNode[R] =
				Triple.objectOf(t)


		//		given tripleTT: TypeTest[Matchable, RDF.Triple[R]] with {
//			override def unapply(s: Matchable): Option[s.type & Triple] =
//				s match
//					//note: this does not compile if we use URI instead of jena.Node_URI
//					case x: (s.type & Triple) => Some(x)
//					case _ => None
//		}

		given Triple: TripleOps with {
			def apply(s: RDF.Node[R], p: RDF.URI[R], o: RDF.Node[R]): RDF.Triple[R] =
				jena.Triple.create(s, p, o).nn
			def untuple(t: RDF.Triple[R]): TripleI =
					(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.Triple[R]): RDF.Node[R] =
				t.getSubject().nn
			def relationOf(t: RDF.Triple[R]): RDF.URI[R] =
				t.getPredicate.asInstanceOf[URI].nn
			def objectOf(t: RDF.Triple[R]): RDF.Node[R] =
				t.getObject().nn
		}

		given Node: NodeOps with
			extension (node: RDF.Node[R])
				def fold[A](
					bnF:  RDF.BNode[R] => A,
					uriF: RDF.URI[R] => A,
					litF: RDF.Literal[R] => A
				): A =
					//considered Jena Visitor, but for some reason it deconstructs the types,
					//annulling potential speed advantage
					if node.isBlank then
						bnF(node.asInstanceOf[Node_Blank])
					else if node.isURI then
						uriF(node.asInstanceOf[Node_URI])
					else if node.isLiteral then
						litF(node.asInstanceOf[Node_Literal])
					else throw new IllegalArgumentException(
						s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
					)
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