package org.w3.banana

import scala.util.Try

trait Ops[Rdf <: RDF]:
	import scala.language.implicitConversions
	import RDF.*

	// todo: this transformation should really be automatically handled by compiler. Report back.
	implicit def lit2Node(lit: Literal[Rdf]): Node[Rdf] = lit.asInstanceOf[Node[Rdf]]
	implicit def uri2Node(uri: URI[Rdf]): Node[Rdf] = uri.asInstanceOf[Node[Rdf]]
	implicit def uri2rUri(uri: URI[Rdf]): rURI[Rdf] = uri.asInstanceOf[rURI[Rdf]]
	implicit def rUri2rNode(uri: rURI[Rdf]): rNode[Rdf] = uri.asInstanceOf[rNode[Rdf]]

	val Graph: GraphOps
	trait GraphOps:
		def empty: Graph[Rdf]
		def apply(triples: Triple[Rdf]*): Graph[Rdf]
		def triplesIn(graph: Graph[Rdf]): Iterable[Triple[Rdf]]
		def graphSize(graph: Graph[Rdf]): Int
		def union(graphs: Seq[Graph[Rdf]]): Graph[Rdf]
		def diff(g1: Graph[Rdf], g2: Graph[Rdf]): Graph[Rdf]
		def isomorphism(left: Graph[Rdf], right: Graph[Rdf]): Boolean

	val rGraph: rGraphOps
	trait rGraphOps:
		def apply(triples: rTriple[Rdf]*): rGraph[Rdf]
		def triplesIn(graph: rGraph[Rdf]): Iterable[rTriple[Rdf]]
		def graphSize(graph: rGraph[Rdf]): Int

	val Triple: TripleOps
	trait TripleOps:
		def apply(s: Node[Rdf], p: URI[Rdf], o: Node[Rdf]): Triple[Rdf]
		def untuple(t: RDF.Triple[Rdf]): TripleI[Rdf]
		def subjectOf(s: Triple[Rdf]): Node[Rdf]
		def relationOf(s: Triple[Rdf]): URI[Rdf]
		def objectOf(s: Triple[Rdf]): Node[Rdf]

	val rTriple: rTripleOps
	trait rTripleOps:
		def apply(s: rNode[Rdf], p: rURI[Rdf], o: rNode[Rdf]): rTriple[Rdf]
		def untuple(t: RDF.Triple[Rdf]): rTripleI[Rdf]
		def subjectOf(s: rTriple[Rdf]): rNode[Rdf]
		def relationOf(s: rTriple[Rdf]): rURI[Rdf]
		def objectOf(s: rTriple[Rdf]): rNode[Rdf]

	object LangLit {
		inline def apply(lex: String, lang: Lang[Rdf]): Literal[Rdf] =
			Literal.langLiteral(lex, lang)
	}
	object TypedLit {
		inline def apply(lex: String, dataTp: URI[Rdf]): Literal[Rdf] =
			Literal.dtLiteral(lex, dataTp)
	}

	val Literal: LiteralOps
	trait LiteralOps:
		def apply(plain: String): Literal[Rdf]
		def apply(lit: LiteralI[Rdf]): Literal[Rdf]
		def unapply(lit: Literal[Rdf]): Option[LiteralI[Rdf]]
		def langLiteral(lex: String, lang: Lang[Rdf]): Literal[Rdf]
		def dtLiteral(lex: String, dataTp: URI[Rdf]): Literal[Rdf]

	val rURI: rURIOps
	trait rURIOps:
		def apply(uriStr: String): rURI[Rdf]
		def asString(uri: rURI[Rdf]): String

	val xsdStr: String = "http://www.w3.org/2001/XMLSchema#string"
	val xsdLangStr: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

	val URI: URIOps
	trait URIOps:
		/** (can) throw an exception (depending on implementation of URI)
		 * different implementations decide to parse at different points, and do
		 * varying quality jobs at that (check).
		 * Need to look at how capability based exceptions could help
		 * https://github.com/lampepfl/dotty/pull/11721/files */
		def apply(uriStr: String): URI[Rdf] = mkUri(uriStr).get
		def mkUri(iriStr: String): Try[URI[Rdf]]
		def asString(uri: URI[Rdf]): String

	val Lang: LangOps
	trait LangOps:
		def apply(lang: String): Lang[Rdf]
		def label(lang: Lang[Rdf]): String

