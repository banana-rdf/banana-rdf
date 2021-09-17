package org.w3.banana.rdf4j

import org.eclipse.rdf4j.model.{BNode as rjBNode, IRI as rjIRI, Literal as rjLiteral, *}
import org.eclipse.rdf4j.model.impl.*
import org.eclipse.rdf4j.model.util.Models
import org.eclipse.rdf4j.query.*
import org.eclipse.rdf4j.query.parser.*
import org.w3.banana.*

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
	override opaque type Lang = String


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

		val Graph = new GraphOps:
			private val emptyGr: RDF.Graph[R] = new LinkedHashModel(0).unmodifiable().nn
			def empty: RDF.Graph[R] = emptyGr
			def apply(triples: RDF.Triple[R]*): RDF.Graph[R] =
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
			def diff(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] =
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
			def apply(triples: RDF.rTriple[R]*): RDF.rGraph[R] =
				Graph(triples*)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				Graph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				Graph.graphSize(graph)

		val Triple = new TripleOps:
			//todo: we should have two types of triples: strict and non-strict (for reasoning)
			//todo: The method that takes a node as subject should not throw an exception, but should
			//   potentially return 1 or two strict triples.
			// warning throws an exception
			def apply(s: RDF.Node[R], p: RDF.URI[R], o: RDF.Node[R]): RDF.Triple[R] =
				s match
					case r: Resource => valueFactory.createStatement(r, p, o).nn
					case p => throw new RuntimeException("makeTriple: in RDF4J, subject " + p.toString + " must be a either URI or BlankNode")
			def untuple(t: RDF.Triple[R]): RDF.TripleI[R] =
					RDF.TripleI(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.Triple[R]): RDF.Node[R] = t.getSubject.nn
			def relationOf(t: RDF.Triple[R]): RDF.URI[R] = t.getPredicate.nn
			def objectOf(t: RDF.Triple[R]): RDF.Node[R] = t.getObject.nn

		val rTriple = new rTripleOps:
			def apply(s: RDF.rNode[R], p: RDF.rURI[R], o: RDF.rNode[R]): RDF.rTriple[R] =
				Triple(s, p, o)
			def untuple(t: RDF.rTriple[R]): RDF.rTripleI[R] =
				RDF.rTripleI(subjectOf(t), relationOf(t), objectOf(t))
			def subjectOf(t: RDF.rTriple[R]): RDF.rNode[R] = Triple.subjectOf(t)
			def relationOf(t: RDF.rTriple[R]): RDF.rURI[R] = Triple.relationOf(t)
			def objectOf(t: RDF.rTriple[R]): RDF.rNode[R] = Triple.objectOf(t)

		val Literal = new LiteralOps:
			private val xsdString = valueFactory.createIRI(xsdStr).nn
			private val xsdLangString = valueFactory.createIRI(xsdLangStr).nn
			import RDF.LiteralI as Lit
			def apply(plain: String): RDF.Literal[R] =
				valueFactory.createLiteral(plain).nn
			def apply(lit: Lit[R]): RDF.Literal[R] = lit match
				case Lit.Plain(text) => apply(text)
				case Lit.`@`(text,lang) => Literal.langLiteral(text,Lang.label(lang))
				case Lit.`^^`(text,tp) => Literal.dtLiteral(text,tp)
			def unapply(lit: RDF.Literal[R]): Option[Lit[R]] =
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
			def langLiteral(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] =
				valueFactory.createLiteral(lex, lang).nn
			def dtLiteral(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] =
				valueFactory.createLiteral(lex, dataTp).nn

		val Lang = new LangOps:
			def apply(lang: String): RDF.Lang[R] = lang
			def label(lang: RDF.Lang[R]): String = lang

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

		val URI = new URIOps:
			//this does throw an exception on non relative URLs!
			def mkUri(iriStr: String): Try[RDF.URI[R]] =
				Try(valueFactory.createIRI(iriStr).nn)

			def asString(uri: RDF.URI[R]): String =
				uri.toString

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
