package org.w3.banana
import RDF.*

import scala.util.Try

trait Ops[Rdf <: RDF]:
	val rdf: Rdf
	import scala.language.implicitConversions

	// using implicits as Conversion requires library users to
	// 1. `import ops.{given,*}`
	// 2. import scala.lang.implicitConversions
	// where this transformation should really be automatically handled.
	implicit def lit2Node(lit: RDF.Literal[Rdf]): RDF.Node[Rdf] = lit.asInstanceOf[RDF.Node[Rdf]]
	implicit def uri2Node(uri: RDF.URI[Rdf]): RDF.Node[Rdf] = uri.asInstanceOf[RDF.Node[Rdf]]

	val Graph: GraphOps
	trait GraphOps:
		def empty: RDF.Graph[Rdf]
		def apply(triples: RDF.Triple[Rdf]*): RDF.Graph[Rdf]
		def triplesIn(graph: RDF.Graph[Rdf]): Iterable[RDF.Triple[Rdf]]
		def graphSize(graph: RDF.Graph[Rdf]): Int
		def union(graphs: Seq[RDF.Graph[Rdf]]): RDF.Graph[Rdf]
		def diff(g1: RDF.Graph[Rdf], g2: RDF.Graph[Rdf]): RDF.Graph[Rdf]
		def isomorphism(left: RDF.Graph[Rdf], right: RDF.Graph[Rdf]): Boolean

	val Triple: TripleOps
	trait TripleOps:
		def apply(s: RDF.Node[Rdf], p: RDF.URI[Rdf], o: RDF.Node[Rdf]): RDF.Triple[Rdf]

	val Literal: LiteralOps
	trait LiteralOps {
		def apply(plain: String): RDF.Literal[Rdf]
		def apply(lit: rdf.LiteralI): RDF.Literal[Rdf]
		def unapply(lit: RDF.Literal[Rdf]): Option[rdf.LiteralI]
		def langLiteral(lex: String, lang: RDF.Lang[Rdf]): RDF.Literal[Rdf]
		def dtLiteral(lex: String, dataTp: RDF.URI[Rdf]): RDF.Literal[Rdf]
	}

	val URI: URIOps
	trait URIOps {
		/** (can) throw an exception (depending on implementation of URI)
		 * different implementations decide to parse at different points, and do
		 * varying quality jobs at that (check).
		 * Need to look at how capability based exceptions could help
		 * https://github.com/lampepfl/dotty/pull/11721/files */
		def apply(uriStr: String): RDF.URI[Rdf] = mkUri(uriStr).get
		def mkUri(iriStr: String): Try[RDF.URI[Rdf]]
		def asString(uri: RDF.URI[Rdf]): String
	}
