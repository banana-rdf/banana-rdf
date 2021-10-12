package org.w3.banana.rdf4j

import org.eclipse.rdf4j.model.{BNode as rjBNode, IRI as rjIRI, Literal as rjLiteral, *}
import org.eclipse.rdf4j.model.impl.*
import org.eclipse.rdf4j.model.util.Models
import org.eclipse.rdf4j.query.*
import org.eclipse.rdf4j.query.parser.*
import org.w3.banana.*

import scala.annotation.targetName
import scala.util.{Success, Try}
import scala.reflect.TypeTest


object Rdf4j extends RDF {

	//rdf4j.Model is modifiable, but we provide no altering methods and always produce new graphs
	override opaque type rGraph = Model
	override opaque type rTriple = Statement
	//type rNode = rjIRI
	override opaque type rURI = rjIRI

	override opaque type Graph = Model
	override opaque type Triple <: Matchable = Statement
	override opaque type Node <: Matchable = Value
	override opaque type URI <: Node = rjIRI
	override opaque type BNode <: Node = rjBNode
	override opaque type Literal <: Node = rjLiteral
	override opaque type Lang <: Matchable = String


//	given uriTT: TypeTest[Node,URI] with {
//		override def unapply(s: Node): Option[s.type & URI] =
//			s match
//				//note: using rjIRI won't compile
//				case x: (s.type & org.eclipse.rdf4j.model.IRI) => Some(x)
//				case _ => None
//	}

//	given literalTT: TypeTest[Node,Literal] with {
//		override def unapply(s: Node): Option[s.type & Literal] =
//			s match
//				//note: this does not compile if we use URI instead of jena.Node_URI
//				case x: (s.type & org.eclipse.rdf4j.model.Literal) => Some(x)
//				case _ => None
//	}

	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
	 * I did not succeed in removing the duplication, as there are Match Type compilation problems.
	 * It does not work to place here the implementations of rdf which can be placed above,
	 * as the RDF.Graph[R] type hides the implementation type (of `graph` field for example) **/
	given ops: Ops[R] with {
		lazy val valueFactory: ValueFactory = SimpleValueFactory.getInstance().nn
		import scala.jdk.CollectionConverters.{given,*}

		given Graph: GraphOps with
			private val emptyGr: RDF.Graph[R] = new LinkedHashModel(0).unmodifiable().nn
			def empty: RDF.Graph[R] = emptyGr
			def apply(triples: Iterable[RDF.Triple[R]]): RDF.Graph[R] =
				val graph = new LinkedHashModel
				triples foreach { t => graph.add(t) }
				graph
			def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] =
				graph.asScala.to(Iterable)
			def graphSize(graph: RDF.Graph[R]): Int = graph.size()
			def union(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] =
				graphs match
					case Seq(x) => x
					case _ =>
						val graph = new LinkedHashModel
						graphs.foreach(graph.addAll(_))
						graph
			def difference(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
				val graph = new LinkedHashModel
				triplesIn(g1) foreach { triple =>
					if !g2.contains(triple) then graph.add(triple)
				}
				graph
			def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
				//note: if we make sure that the opaque Graph, never contains contexts,
				//  then this is all we need to do. Otherwise we need to strip contexts.
				Models.isomorphic(left, right)

		val rGraph = new rGraphOps:
			def empty: RDF.rGraph[R] = Graph.empty
			def apply(triples: Iterable[RDF.rTriple[R]]): RDF.rGraph[R] =
				Graph(triples)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				Graph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				Graph.graphSize(graph).toInt

//		given tripleTT: TypeTest[Matchable, RDF.Triple[R]] with {
//			override def unapply(s: Matchable): Option[s.type & Triple] =
//				s match
//					//note: this does not compile if we use URI instead of jena.Node_URI
//					case x: (s.type & Triple) => Some(x)
//					case _ => None
//		}

		given Triple: TripleOps with
			import RDF.Statement as St
			def apply(s: St.Subject[R], p: St.Relation[R], o: St.Object[R]): RDF.Triple[R] =
				valueFactory.createStatement(s, p, o).nn
			def untuple(t: RDF.Triple[R]): TripleI =
					(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.Triple[R]): St.Subject[R] =
				//todo: this asInstanceOf should not be here
				t.getSubject().nn.asInstanceOf[St.Subject[R]]
			def relationOf(t: RDF.Triple[R]): St.Relation[R] = t.getPredicate().nn
			def objectOf(t: RDF.Triple[R]): St.Object[R] =
				//todo: this asInstanceOf should not be here
				t.getObject().nn.asInstanceOf[St.Object[R]]
		end Triple

		val rTriple = new rTripleOps:
			import RDF.rStatement as rSt
			def apply(s: rSt.Subject[R], p: rSt.Relation[R], o: rSt.Object[R]): RDF.rTriple[R] =
				Triple(s, p, o)
			def untuple(t: RDF.rTriple[R]): rTripleI =
				(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.rTriple[R]): rSt.Subject[R] = Triple.subjectOf(t)
			def relationOf(t: RDF.rTriple[R]): rSt.Relation[R] = Triple.relationOf(t)
			def objectOf(t: RDF.rTriple[R]): rSt.Object[R] = Triple.objectOf(t)
		end rTriple

		given Statement: StatementOps with
			extension (subj: RDF.Statement.Subject[R])
				def fold[A](uriFnct: RDF.URI[R] => A, bnFcnt: RDF.BNode[R] => A): A =
					if subj.isBNode() then
						bnFcnt(subj.asInstanceOf[rjBNode])
					else uriFnct(subj.asInstanceOf[rjIRI])

		given Node: NodeOps with
			extension (node: RDF.Node[R])
				def fold[A](
					uriF: RDF.URI[R] => A,
					bnF: RDF.BNode[R] => A,
					litF: RDF.Literal[R] => A
				): A =
					if node.isBNode() then
						bnF(node.asInstanceOf[rjBNode])
					else if node.isIRI() then
						uriF(node.asInstanceOf[rjIRI])
					else if node.isLiteral() then
						litF(node.asInstanceOf[rjLiteral])
					else throw new IllegalArgumentException(
						s"node.fold() received `$node` which is neither a BNode, URI or Literal. Please report."
					)

		given BNode: BNodeOps with
			def apply(s: String): RDF.BNode[R] = valueFactory.createBNode(s).nn
			def apply(): RDF.BNode[R] =  valueFactory.createBNode().nn
			extension (bn: RDF.BNode[R])
				def label: String = bn.getID().nn
		end BNode

		given Literal: LiteralOps with
			private val xsdString = valueFactory.createIRI(xsdStr).nn
			private val xsdLangString = valueFactory.createIRI(xsdLangStr).nn

			import LiteralI as Lit

			def apply(plain: String): RDF.Literal[R] =
				valueFactory.createLiteral(plain).nn
			def apply(lit: LiteralI): RDF.Literal[R] = lit match
				case Lit.Plain(text) => apply(text)
				case Lit.`@`(text, lang) => Literal(text, Lang.label(lang))
				case Lit.`^^`(text, tp) => Literal(text, tp)

			def unapply(x: Matchable): Option[Lit] =
				x match
					case lit: Literal =>
						val lex: String = lit.getLabel.nn
						val dt: RDF.URI[R] = lit.getDatatype.nn
						val lang: java.util.Optional[String] = lit.getLanguage.nn
						if (lang.isEmpty) then
						//todo: this comparison could be costly, check
							if dt == xsdString then Some(Lit.Plain(lex))
							else Some(Lit.^^(lex, dt))
						else if dt == xsdLangString then
							Some(Lit.`@`(lex, Lang(lang.get().nn)))
						else None
					case _ => None

			@targetName("langLit")
			def apply(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] =
				valueFactory.createLiteral(lex, lang).nn

			@targetName("dataTypeLit")
			def apply(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] =
				valueFactory.createLiteral(lex, dataTp).nn

			extension (lit: RDF.Literal[R])
				def text: String = lit.getLabel.nn
		end Literal

		override given literalTT: TypeTest[Matchable, RDF.Literal[R]] with {
			override def unapply(s: Matchable): Option[s.type & RDF.Literal[R]] =
				s match
					//note: this does not compile if we use URI instead of jena.Node_URI
					case x: (s.type & org.eclipse.rdf4j.model.Literal) => Some(x)
					case _ => None
		}

		given Lang: LangOps with {
			def apply(lang: String): RDF.Lang[R] = lang
			extension (lang: RDF.Lang[R])
				def label: String =  lang
		}

		val rURI = new rURIOps:
			def apply(iriStr: String): RDF.rURI[R] =
				new rjIRI {
					override def equals(o: Any): Boolean =
						o.isInstanceOf[rjIRI] && o.asInstanceOf[rjIRI].toString.equals(iriStr)
					def getLocalName: String = iriStr
					def getNamespace: String = ""
					override def hashCode: Int = iriStr.hashCode
					override def toString: String = iriStr
					def stringValue: String = iriStr
				}
			def asString(uri: RDF.rURI[R]): String = uri.toString

		given URI: URIOps with
			//this does throw an exception on non relative URLs!
			def mkUri(iriStr: String): Try[RDF.URI[R]] =
				Try(valueFactory.createIRI(iriStr).nn)
			def asString(uri: RDF.URI[R]): String = uri.toString
		end URI

	}

	// mutable graphs
//	type MGraph = Model
//
//	// types for the graph traversal API
//	type NodeMatch = Value
//	type NodeAny = Null
//	type NodeConcrete = Value
//
//	// types related to Sparql
//	type Query = ParsedQuery
//	type SelectQuery = ParsedTupleQuery
//	type ConstructQuery = ParsedGraphQuery
//	type AskQuery = ParsedBooleanQuery
//
//	//FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
//	type UpdateQuery = Rdf4jParseUpdate
//
//	type Solution = BindingSet
//	// instead of TupleQueryResult so that it's eager instead of lazy
//	type Solutions = Vector[BindingSet]
}
