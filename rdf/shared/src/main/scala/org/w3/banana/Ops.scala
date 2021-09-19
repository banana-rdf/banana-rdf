package org.w3.banana

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

trait Ops[Rdf <: RDF]:
	import scala.language.implicitConversions
	import RDF.*
	export LiteralI.*

   // needed to help inferencing
	// todo: this transformation should really be automatically handled by compiler. Report back.
	implicit def lit2Node(lit: Literal[Rdf]): Node[Rdf] = lit.asInstanceOf[Node[Rdf]]
	implicit def uri2Node(uri: URI[Rdf]): Node[Rdf] = uri.asInstanceOf[Node[Rdf]]
	implicit def bnode2Node(bn: BNode[Rdf]): Node[Rdf] = bn.asInstanceOf[Node[Rdf]]
	implicit def uri2rUri(uri: URI[Rdf]): rURI[Rdf] = uri.asInstanceOf[rURI[Rdf]]
	implicit def rUri2rNode(uri: rURI[Rdf]): rNode[Rdf] = uri.asInstanceOf[rNode[Rdf]]

	// interpretation types to help consistent pattern matching across implementations

	enum LiteralI(val text: String):
		case Plain(override val text: String) extends LiteralI(text)
		case `@`(override val text: String, lang: Lang[Rdf]) extends LiteralI(text)
		case ^^(override val text: String, dataTp: URI[Rdf]) extends LiteralI(text)
	type TripleI = (Node[Rdf], URI[Rdf], Node[Rdf])
	type rTripleI = (rNode[Rdf], rURI[Rdf], rNode[Rdf])

	//implementations


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

//	given tripleTT: TypeTest[Matchable, Triple[Rdf]]

	given Triple: TripleOps
	trait TripleOps:
		def apply(s: Node[Rdf], p: URI[Rdf], o: Node[Rdf]): Triple[Rdf]
		def unapply(t: Triple[Rdf]): Option[TripleI] = Some(untuple(t))
		def untuple(t: Triple[Rdf]): TripleI
		def subjectOf(s: Triple[Rdf]): Node[Rdf]
		def relationOf(s: Triple[Rdf]): URI[Rdf]
		def objectOf(s: Triple[Rdf]): Node[Rdf]
		extension (triple: Triple[Rdf])
			def subj: Node[Rdf] = subjectOf(triple)
			def rel: URI[Rdf]   = relationOf(triple)
			def obj: Node[Rdf]  = objectOf(triple)

	val rTriple: rTripleOps
	trait rTripleOps:
		def apply(s: rNode[Rdf], p: rURI[Rdf], o: rNode[Rdf]): rTriple[Rdf]
		def unapply(t: RDF.Triple[Rdf]): Option[rTripleI] = Some(untuple(t))
		def untuple(t: RDF.Triple[Rdf]): rTripleI
		def subjectOf(s: rTriple[Rdf]): rNode[Rdf]
		def relationOf(s: rTriple[Rdf]): rURI[Rdf]
		def objectOf(s: rTriple[Rdf]): rNode[Rdf]
		//todo? should we only have the extension functions?
		extension (rtriple: rTriple[Rdf])
			def rsubj: rNode[Rdf] = subjectOf(rtriple)
			def rrel: rURI[Rdf]   = relationOf(rtriple)
			def robj: rNode[Rdf]  = objectOf(rtriple)
	end rTripleOps

	object LangLit {
		inline def apply(lex: String, lang: Lang[Rdf]): Literal[Rdf] =
			Literal.langLiteral(lex, lang)
	}
	object TypedLit {
		inline def apply(lex: String, dataTp: URI[Rdf]): Literal[Rdf] =
			Literal.dtLiteral(lex, dataTp)
	}

	given Node: NodeOps
	trait NodeOps:
		extension (node: Node[Rdf])
			def fold[A](
				bnF:  BNode[Rdf] => A,
				uriF: URI[Rdf] => A,
				litF: Literal[Rdf] => A
			): A
	end NodeOps

	//todo? should a BNode be part of a Graph (or DataSet) as per Benjamin Braatz's thesis?
	given BNode: BNodeOps
	trait BNodeOps:
		def apply(s: String): BNode[Rdf]
		def apply(): BNode[Rdf]
		extension (bn: BNode[Rdf])
			def label: String
	end BNodeOps

	given Literal: LiteralOps
	trait LiteralOps:
		def apply(plain: String): Literal[Rdf]
		def apply(lit: LiteralI): Literal[Rdf]
		def unapply(lit: Matchable): Option[LiteralI]
		def langLiteral(lex: String, lang: Lang[Rdf]): Literal[Rdf]
		def dtLiteral(lex: String, dataTp: URI[Rdf]): Literal[Rdf]

		extension (lit: Literal[Rdf])
			def text: String
			// this can be implemented more efficiently in individual subclasses by
			//avoiding going through the intermdiate LiteralI type. Indeed the
			//unapply should be implemented in terms of this function
			def fold[A](
				plainF: String => A,
				langF: (String, Lang[Rdf]) => A,
				dtTypeF: (String, URI[Rdf]) => A
			): A =
				unapply(lit).get match
				case Plain(t) => plainF(t)
				case t `@` lang => langF(t,lang)
				case t ^^ dt => dtTypeF(t,dt)

		extension (str: String)
			@targetName("dt")
			infix def ^^(dtType: URI[Rdf]): Literal[Rdf] =
				dtLiteral(str, dtType)
			@targetName("lang")
			infix def `@`(lang: Lang[Rdf]): Literal[Rdf] =
				langLiteral(str, lang)
	end LiteralOps

	given literalTT: TypeTest[Matchable, RDF.Literal[Rdf]]

	val rURI: rURIOps
	trait rURIOps:
		def apply(uriStr: String): rURI[Rdf]
		def asString(uri: rURI[Rdf]): String

	val xsdStr: String = "http://www.w3.org/2001/XMLSchema#string"
	val xsdLangStr: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

	given URI: URIOps
	trait URIOps:
		/** (can) throw an exception (depending on implementation of URI)
		 * different implementations decide to parse at different points, and do
		 * varying quality jobs at that (check).
		 * Need to look at how capability based exceptions could help
		 * https://github.com/lampepfl/dotty/pull/11721/files */
		def apply(uriStr: String): URI[Rdf] = mkUri(uriStr).get
		def mkUri(iriStr: String): Try[URI[Rdf]]
		def asString(uri: URI[Rdf]): String
		extension (uri: URI[Rdf])
			def string: String = asString(uri)


	given Lang: LangOps
	trait LangOps:
		def apply(name: String): Lang[Rdf]
		def unapply(lang: Lang[Rdf]): Option[String] = Some(lang.label)
		extension (lang: RDF.Lang[Rdf])
			def label: String


end Ops

