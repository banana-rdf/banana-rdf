package org.w3.banana

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

/**
 * Main RDF types.
 * Implementations will mostly use opaque types, so we need to provide the operations too.
 * todo: how can one modularise this, while taking into account that implementations will
 *   be using opaque types?
 *
 * rURI, rTriple, rGraph are all possibly relative versions of respectively a URI, Triple and Graph.
 * Relative Graphs have fewer applicable methods: they cannot be unioned with another graph for example.
 */
trait RDF:
	type R <: RDF & Singleton

	type rGraph 					// graphs with triples with relative URLs
	type rTriple					// triples with relative URLs
	type rNode = rURI | Node  	// relative node
	type rURI 					   // relative URLs

	type Graph     			   // graphs with no triples with relative URLs
	type Triple  <: Matchable	// triples with no relative URLs
	type Node <: Matchable
	type URI <: Node
	type BNode <: Node
	type Literal <: Node
	type Lang

	//interface types: the way we use to present the types for pattern matching
	type TripleI = (Node, URI, Node)
	type rTripleI = (rNode, rURI, rNode)

	// pre-interpreted literal type for pattern matching
	// it would also be reasonable to have an interpreted types to Int, Long, BigInt, etc...
	//but that would be one step further in interpretation
	enum LiteralI(text: String) {
		case Plain(text: String) extends LiteralI(text)
		case `@`(text: String, lang: Lang) extends LiteralI(text)
		case ^^(text: String, dataTp: URI) extends LiteralI(text)
	}

	val rTriple : rTripleOps

	trait rTripleOps {
		def apply(sub: rNode, rel: rURI, obj: rNode): rTriple
		def subjectOf(triple: rTriple): rNode
		def relationOf(triple: rTriple): rURI
		def objectOf(triple: rTriple): rNode
	}

	//we need all implementations to have a given tripleOps available
	val Triple: TripleOps

	// does not work with Simple implementation below
	//given tripleTT: TypeTest[Any,Triple]

	/** Triple interface */
	trait TripleOps {
		def apply(subj: Node, rel: URI, obj: Node): Triple
		def unapply(t: Triple): Option[TripleI] = Some(untuple(t))
		def untuple(t: Triple): TripleI
		def subjectOf(triple: Triple): Node
		def relationOf(triple: Triple): URI
		def objectOf(triple: Triple): Node
	}

	val rURI: rURIOps

	trait rURIOps {
		def apply(rUriStr: String): rURI
		def asString(uri: rURI): String
	}

	//todo: we should add Relative URI type
	val URI: URIOps

	trait URIOps {
		/** (can) throw an exception (depending on implementation of URI)
		 * different implementations decide to parse at different points, and do
		 * varying quality jobs at that (check).
		 * Need to look at how capability based exceptions could help
		 * https://github.com/lampepfl/dotty/pull/11721/files */
		def apply(uriStr: String): URI = mkUri(uriStr).get
		def mkUri(iriStr: String): Try[URI]
		def asString(uri: URI): String
	}

	extension (uri: URI)
		def asString: String = URI.asString(uri)
	//and then a lot of other methods to get path, domain, etc...


	given uriTT: TypeTest[Node,URI]

	//		val BNode : BNode
	val Literal: LiteralOps

	trait LiteralOps {
		import LiteralI.*
		def apply(plain: String): Literal
		def apply(lit: LiteralI): Literal = lit match
			case Plain(text) => apply(text)
			case `@`(text,lang) => langLiteral(text,lang)
			case `^^`(text,tp) => dtLiteral(text,tp)
		def unapply(lit: Literal): Option[LiteralI]
		def langLiteral(lex: String, lang: Lang): Literal
		def dtLiteral(lex: String, dataTp: URI): Literal
	}

	given literalTT: TypeTest[Node,Literal]

	val Lang: LangOps
	//todo Lang, should contain all the supported languages, plus an unsafe way of creating new ones
	trait LangOps {
		def apply(lang: String): Lang
		def label(lang: Lang): String
	}

	//this can be an external import
	object LiteralSyntax:
		extension (str: String)
			@targetName("dt")
			infix def ^^(dtType: URI): Literal = Literal.dtLiteral(str,dtType)
			@targetName("lang")
			infix def `@`(lang: Lang): Literal = Literal.langLiteral(str,lang)
	end LiteralSyntax

	extension (lang: Lang)
		def label: String =  Lang.label(lang)

	object LangLit {
		inline def apply(lex: String, lang: Lang): Literal = Literal.langLiteral(lex, lang)
	}
	object TypedLit {
		inline def apply(lex: String, dataTp: URI): Literal = Literal.dtLiteral(lex, dataTp)
	}

	val rGraph : rGraphOps
	trait rGraphOps {
		def apply(triples: rTriple*): rGraph
		def triplesIn(graph: rGraph): Iterable[rTriple]
		def graphSize(graph: rGraph): Int
	}

	val Graph : GraphOps
	trait GraphOps {
		def empty: Graph
		def apply(triples: Triple*): Graph
		def triplesIn(graph: Graph): Iterable[Triple]
		def graphSize(graph: Graph): Int
		def union(graphs: Seq[Graph]): Graph
		def diff(g1: Graph, g2: Graph): Graph
		def isomorphism(left: Graph, right: Graph): Boolean
	}
//		def ANY: NodeAny
//		implicit def toConcreteNodeMatch(node: Rdf#Node): Rdf#NodeMatch
//		def foldNodeMatch[T](nodeMatch: Rdf#NodeMatch)(funANY: => T, funNode: Rdf#Node => T): T
//		def find(graph: Rdf#Graph, subject: Rdf#NodeMatch, predicate: Rdf#NodeMatch, objectt: Rdf#NodeMatch): Iterator[Rdf#Triple]


	given ops: Ops[R]

end RDF


// remain to be done:
//  // mutable graphs
//  type MGraph <: AnyRef
//
//  // types for the graph traversal API
//  type NodeMatch
//  type NodeAny <: NodeMatch
//
//  // types related to Sparql
//  type Query
//  type SelectQuery <: Query
//  type ConstructQuery <: Query
//  type AskQuery <: Query
//  type UpdateQuery
//  type Solution
//  type Solutions

/**
 * The idea of using match types by @neko-kai
 * https://github.com/lampepfl/dotty/issues/13416
 */
object RDF {
	type RDFObj = RDF //& Singleton // Is the "& Singleton" of use?

	type rTriple[R <: RDFObj] = R match
		case GetRelTriple[t] => t

	type Triple[R <: RDFObj] = R match
		case GetTriple[t] => t

	type rNode[R <: RDFObj] = R match
		case GetRelNode[n] => n

	type Node[R <: RDFObj] = R match
		case GetNode[n] => n

	type rURI[R <: RDFObj] = R match
		case GetRelURI[ru] => ru

	type URI[R <: RDFObj] = R match
		case GetURI[u] => u

	type rGraph[R <: RDFObj] = R match
		case GetRelGraph[g] => g

	type Graph[R <: RDFObj] = R match
		case GetGraph[g] => g

	type Literal[R <: RDFObj] = R match
		case GetLiteral[l] => l

	type Lang[R <: RDFObj] = R match
		case GetLang[l] => l

	type GetRelURI[U] = RDF { type rURI = U }
	type GetURI[U] = RDF { type URI = U }
	type GetRelNode[N] = RDF { type rNode = N }
	type GetNode[N] = RDF { type Node = N }
	type GetLiteral[L] = RDF { type Literal = L }
	type GetLang[L] = RDF { type Lang = L }
	type GetRelTriple[T] = RDF { type rTriple = T }
	type GetTriple[T] = RDF { type Triple = T }
	type GetRelGraph[G] = RDF { type rGraph = G }
	type GetGraph[G] = RDF { type Graph = G }
}


