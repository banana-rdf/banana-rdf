package org.w3.banana

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

/**
 * Main RDF types.
 * Implementations will mostly use opaque types, so we need to provide the operations too.
 * todo: how can one modularise this, while taking into account that implementations will
 *   be using opaque types
 */
trait RDF:
  // types related to the RDF datamodel
	type Graph
	type Triple <: Matchable
	type Node <: Matchable
	type URI <: Node
	type BNode <: Node
	type Literal <: Node
	type Lang
	//interface types: the way we use to present the types for pattern matching
	type TripleI = (Node, URI, Node)

	// pre-interpreted literal type for pattern matching
	// it would also be reasonable to have an interpreted types to Int, Long, BigInt, etc...
	//but that would be one step further in interpretation
	enum LiteralI(text: String) {
		case Plain(text: String) extends LiteralI(text)
		case `@`(text: String, lang: Lang) extends LiteralI(text)
		case ^^(text: String, dataTp: URI) extends LiteralI(text)
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

	extension (triple: Triple)
		def subj: Node = Triple.subjectOf(triple)
		def rel: URI = Triple.relationOf(triple)
		def obj: Node = Triple.objectOf(triple)

	//todo: we should add Relative URI type
	val URI: URIOps

	//		val Node : Node
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

	// this method allows for multiple implementation of the Graph object
	val Graph : GraphOps
	trait GraphOps {
		def empty: Graph
		def apply(triples: Triple*): Graph
		def triplesIn(graph: Graph): Iterable[Triple]
	}

	extension (graph: Graph)
		def triples: Iterable[Triple] = Graph.triplesIn(graph)

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

object RDF {
	type RDFObj = RDF & Singleton
	type Triple[R <: RDFObj] = R match
		case GetTriple[t] => t

	type Node[R <: RDFObj] = R match
		case GetNode[n] => n

	type URI[R <: RDFObj] = R match
		case GetURI[u] => u

	type Graph[R <: RDFObj] = R match
		case GetGraph[g] => g

	type Literal[R <: RDFObj] = R match
		case GetLiteral[l] => l

	type GetNode[N] = RDF { type Node = N }
	type GetLiteral[L] = RDF { type Literal = L }
	type GetURI[U] = RDF { type URI = U }
	type GetTriple[T] = RDF { type Triple = T }
	type GetGraph[G] = RDF { type Graph = G }
}


