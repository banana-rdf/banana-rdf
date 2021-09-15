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
	type R = Rdf4j.type
	lazy val valueFactory: ValueFactory = SimpleValueFactory.getInstance().nn

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

	override val rTriple = new rTripleOps {
		def apply(subj: rNode, rel: rURI, obj: rNode): rTriple = Triple(subj,rel,obj)
		def subjectOf(triple: rTriple): rNode = Triple.subjectOf(triple)
		def relationOf(triple: rTriple): rURI = Triple.relationOf(triple)
		def objectOf(triple: rTriple): rNode = Triple.objectOf(triple)
	}

	override val Triple: TripleOps = new TripleOps  {

		//todo: we should have two types of triples: strict and non-strict (for reasoning)
		//todo: The method that takes a node as subject should not throw an exception, but should
		//   potentially return 1 or two strict triples.
		def apply(subj: Node, rel: URI, obj: Node): Triple =
			subj match
			case res: Resource => valueFactory.createStatement(res, rel, obj).nn
			case p => throw new RuntimeException("makeTriple: in RDF4J, subject " + p.toString + " must be a either URI or BlankNode")

		override inline
		def untuple(t: Triple): TripleI =
			(subjectOf(t), relationOf(t), objectOf(t))
		override inline
		def subjectOf(triple: Triple): Node = triple.getSubject.nn
		override inline
		def relationOf(triple: Triple): URI = triple.getPredicate().nn
		override inline
		def objectOf(triple: Triple): Node  = triple.getObject().nn
	}

	given uriTT: TypeTest[Node,URI] with {
		override def unapply(s: Node): Option[s.type & URI] =
			s match
				//note: using rjIRI won't compile
				case x: (s.type & org.eclipse.rdf4j.model.IRI) => Some(x)
				case _ => None
	}

	override val rURI = new rURIOps {
		def apply(rUriStr: String): rURI = URI(rUriStr)
		def asString(u: rURI): String = u.toString
	}

	override val URI : URIOps = new URIOps  {

		override def apply(uriStr: String): URI = makeUri(uriStr)
		override
		def mkUri(iriStr: String): Try[URI] = Success(makeUri(iriStr))

		/**
		 * we provide our own builder for Rdf4's URI to relax the constraint "the URI must be absolute"
		 * this constraint becomes relevant only when you add the URI to a Sesame store.
		 * todo: we need a type for a relative URI and one for a non relative URI
		 */
		def makeUri(iriStr: String): rjIRI = {
			try {
				valueFactory.createIRI(iriStr).nn
			} catch {
				case iae: IllegalArgumentException =>
					new rjIRI {
						override def equals(o: Any): Boolean = o.isInstanceOf[rjIRI] && o.asInstanceOf[rjIRI].toString.equals(iriStr)
						def getLocalName: String = iriStr
						def getNamespace: String = ""
						override def hashCode: Int = iriStr.hashCode
						override def toString: String = iriStr
						def stringValue: String = iriStr
					}
			}
		}
		override inline
		def asString(uri: URI): String = uri.toString
	}

	override val Literal: LiteralOps = new LiteralOps {
		//todo: move these two to a public space as needed in each implementation
		private val xsdString: URI = URI("http://www.w3.org/2001/XMLSchema#string")
		private val xsdLangString: URI = URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")

		def apply(plain: String): Literal =
			valueFactory.createLiteral(plain).nn

		def dtLiteral(lex: String, dataTp: URI): Literal =
			valueFactory.createLiteral(lex, dataTp).nn

		def langLiteral(lex: String, lang: Lang): Literal =
			valueFactory.createLiteral(lex, lang).nn

		//note: in this implementation we have to make potential string comparisons,
		//which could be costly. Optimise or think of passing the data as in b-rdf 0.8
		def unapply(lit: Literal): Option[LiteralI] =
			import LiteralI.*
			val lex: String = lit.getLabel.nn
			val dt: URI = lit.getDatatype.nn
			val lang: java.util.Optional[String] = lit.getLanguage.nn
			if (lang.isEmpty) then
				//todo: this comparison could be costly, check
				if dt == xsdString then Some(Plain(lex))
				else Some(^^(lex, dt))
			else if dt == xsdLangString then
				Some(`@`(lex, lang.get().nn))
			else None
	}

	given literalTT: TypeTest[Node,Literal] with {
		override def unapply(s: Node): Option[s.type & Literal] =
			s match
				//note: this does not compile if we use URI instead of jena.Node_URI
				case x: (s.type & org.eclipse.rdf4j.model.Literal) => Some(x)
				case _ => None
	}

	override val Lang: LangOps =  new LangOps {
		override inline def apply(lang: String): Lang = lang
		override inline def label(lang: Lang): String = lang
	}

	override val rGraph = new rGraphOps {
		def apply(triples: rTriple*): rGraph = Graph(triples*)
		def triplesIn(graph: rGraph): Iterable[rTriple] = Graph.triplesIn(graph)
		def graphSize(graph: rGraph): Int = Graph.graphSize(graph)
	}

	override val Graph: GraphOps = new GraphOps {
		private val emptyGr: Graph = new LinkedHashModel(0).unmodifiable().nn
		override inline def empty: Graph = emptyGr
		override inline def apply(triples: Triple*): Graph =
			val graph = new LinkedHashModel
			triples foreach { t => graph.add(t) }
			graph

		import scala.jdk.CollectionConverters.{given,*}
		//
		//how should one pass on the information that the Iterable is closeable?
		// https://stackoverflow.com/questions/69153609/is-there-a-cross-platform-autocloseable-iterable-solution-for-scala
		override def triplesIn(graph: Graph): Iterable[Triple] =
			graph.asScala.to(Iterable)

		override inline
		def graphSize(graph: Graph): Int =
			graph.size()

		def union(graphs: Seq[Graph]): Graph =
			graphs match
			case Seq(x) => x
			case _ =>
				val graph = new LinkedHashModel
				graphs.foreach(graph.addAll(_))
				graph

		def diff(g1: Graph, g2: Graph): Graph =
			val graph = new LinkedHashModel
			triplesIn(g1) foreach { triple =>
				if !g2.contains(triple) then graph.add(triple)
			}
			graph

		override inline
		def isomorphism(left: Graph, right: Graph): Boolean =
			//note: if we make sure that the opaque Graph, never contains contexts,
			//  then this is all we need to do. Otherwise we need to strip contexts.
			Models.isomorphic(left, right)
	}

	/**
	 * Here we build up the methods functions allowing RDF.Graph[R] notation to be used.
	 *
	 * This will be the same code in every singleton implementation of RDF.
	 * I did not succeed in removing the duplication, as there are Match Type compilation problems.
	 * It does not work to place here the implementations of rdf which can be placed above,
	 * as the RDF.Graph[R] type hides the implementation type (of `graph` field for example) **/
	given ops: Ops[R] with {
		val rdf = Rdf4j

		val Graph = new GraphOps:
			def empty: RDF.Graph[R] = rdf.Graph.empty
			def apply(triples: RDF.Triple[R]*): RDF.Graph[R] = rdf.Graph(triples*)
			def triplesIn(graph: RDF.Graph[R]): Iterable[RDF.Triple[R]] = rdf.Graph.triplesIn(graph)
			def graphSize(graph: RDF.Graph[R]): Int = rdf.Graph.graphSize(graph)
			def union(graphs: Seq[RDF.Graph[R]]): RDF.Graph[R] = rdf.Graph.union(graphs)
			def diff(g1: RDF.Graph[R], g2: RDF.Graph[R]): RDF.Graph[R] = rdf.Graph.diff(g1,g2)
			def isomorphism(left: RDF.Graph[R], right: RDF.Graph[R]): Boolean =
				rdf.Graph.isomorphism(left,right)

		val rGraph = new rGraphOps:
			def apply(triples: RDF.rTriple[R]*): RDF.rGraph[R] =
				rdf.rGraph(triples*)
			def triplesIn(graph: RDF.rGraph[R]): Iterable[RDF.rTriple[R]] =
				rdf.rGraph.triplesIn(graph)
			def graphSize(graph: RDF.rGraph[R]): Int =
				rdf.rGraph.graphSize(graph)

		val Triple = new TripleOps:
			def apply(s: RDF.Node[R], p: RDF.URI[R], o: RDF.Node[R]): RDF.Triple[R] =
				rdf.Triple(s, p, o)
			def subjectOf(t: RDF.Triple[R]): RDF.Node[R] = rdf.Triple.subjectOf(t)
			def relationOf(t: RDF.Triple[R]): RDF.URI[R] = rdf.Triple.relationOf(t)
			def objectOf(t: RDF.Triple[R]): RDF.Node[R] = rdf.Triple.objectOf(t)

		val rTriple = new rTripleOps:
			def apply(s: RDF.rNode[R], p: RDF.rURI[R], o: RDF.rNode[R]): RDF.rTriple[R] =
				rdf.rTriple(s, p, o)
			def subjectOf(t: RDF.rTriple[R]): RDF.rNode[R] = rdf.rTriple.subjectOf(t)
			def relationOf(t: RDF.rTriple[R]): RDF.rURI[R] = rdf.rTriple.relationOf(t)
			def objectOf(t: RDF.rTriple[R]): RDF.rNode[R] = rdf.rTriple.objectOf(t)

		val Literal = new LiteralOps:
			import LiteralI.*
			def apply(plain: String): RDF.Literal[R] = rdf.Literal(plain)
			def apply(lit: LiteralI): RDF.Literal[R] = lit match
				case Plain(text) => apply(text)
				case `@`(text,lang) => langLiteral(text,lang)
				case `^^`(text,tp) => dtLiteral(text,tp)
			def unapply(lit: RDF.Literal[R]): Option[LiteralI] = rdf.Literal.unapply(lit)
			def langLiteral(lex: String, lang: RDF.Lang[R]): RDF.Literal[R] = rdf.Literal.langLiteral(lex,lang)
			def dtLiteral(lex: String, dataTp: RDF.URI[R]): RDF.Literal[R] = rdf.Literal.dtLiteral(lex,dataTp)

		val rURI = new rURIOps:
			def apply(uriStr: String): RDF.rURI[R] = rdf.rURI(uriStr)
			def asString(uri: RDF.rURI[R]): String = rdf.rURI.asString(uri)

		val URI = new URIOps:
			def mkUri(iriStr: String): Try[RDF.URI[R]] = rdf.URI.mkUri(iriStr)
			def asString(uri: RDF.URI[R]): String = rdf.URI.asString(uri)

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
