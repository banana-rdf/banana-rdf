package org.w3.banana

import org.w3.banana.RDF.Graph

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

trait Ops[Rdf <: RDF]:
	import scala.language.implicitConversions
	import RDF.*
	import RDF.Statement as St
	export LiteralI.*

   // needed to help inferencing
	// todo: this transformation should really be automatically handled by compiler. Report back.
	implicit def lit2Node(lit: Literal[Rdf]): Node[Rdf] = lit.asInstanceOf[Node[Rdf]]
	implicit def uri2Node(uri: URI[Rdf]): Node[Rdf] = uri.asInstanceOf[Node[Rdf]]
	implicit def bnode2Node(bn: BNode[Rdf]): Node[Rdf] = bn.asInstanceOf[Node[Rdf]]
	implicit def uri2rUri(uri: URI[Rdf]): rURI[Rdf] = uri.asInstanceOf[rURI[Rdf]]
	implicit def rUri2rNode(uri: rURI[Rdf]): rNode[Rdf] = uri.asInstanceOf[rNode[Rdf]]

	//conversions for position types
	implicit def obj2Node(obj: St.Object[Rdf]): Node[Rdf] = obj.asInstanceOf[Node[Rdf]]
	//note:  if we use the conversion below, then all the code needs to import scala.language.implicitConversions
	//	given Conversion[St.Object[Rdf],RDF.Node[Rdf]] with
	//		def apply(obj: St.Object[Rdf]): RDF.Node[Rdf] =  obj.asInstanceOf[Node[Rdf]]

	// interpretation types to help consistent pattern matching across implementations

	enum LiteralI(val text: String):
		case Plain(override val text: String) extends LiteralI(text)
		case `@`(override val text: String, lang: Lang[Rdf]) extends LiteralI(text)
		case ^^(override val text: String, dataTp: URI[Rdf]) extends LiteralI(text)
	type TripleI = (Node[Rdf], URI[Rdf], Node[Rdf])
	type rTripleI = (rNode[Rdf], rURI[Rdf], rNode[Rdf])

	//implementations

	given Graph: GraphOps
	trait GraphOps:
		def empty: Graph[Rdf]
		def apply(triples: Iterable[Triple[Rdf]]): Graph[Rdf]
		def apply(head: Triple[Rdf], tail: Triple[Rdf]*): Graph[Rdf] =
			val it: Iterable[Triple[Rdf]] = Iterable[Triple[Rdf]](tail.prepended(head)*)
			Graph.apply(it)
		//todo: remove all the protected methods after moving code to extension.
		protected
		def triplesIn(graph: Graph[Rdf]): Iterable[Triple[Rdf]]
		protected
		def graphSize(graph: Graph[Rdf]): Int
		protected
		def union(graphs: Seq[Graph[Rdf]]): Graph[Rdf]
		protected
		def difference(g1: Graph[Rdf], g2: Graph[Rdf]): Graph[Rdf]
		protected
		def isomorphism(left: Graph[Rdf], right: Graph[Rdf]): Boolean
		extension (graph: Graph[Rdf])
			@targetName("iso")
			def ≅ (other: Graph[Rdf]): Boolean = isomorphism(graph,other)
			infix def isomorphic(other: Graph[Rdf]): Boolean = isomorphism(graph,other)
			def diff(other: Graph[Rdf]): Graph[Rdf] = difference(graph,other)
			def size: Int = graphSize(graph)
			def triples: Iterable[Triple[Rdf]] = triplesIn(graph)
			def union(graphs: Graph[Rdf]*): Graph[Rdf] = Graph.union(graph +: graphs )
			def +(triple: Triple[Rdf]): Graph[Rdf] = Graph.union(Seq(graph, Graph(triple)))

	val rGraph: rGraphOps
	trait rGraphOps:
		def empty: rGraph[Rdf]
		def apply(triples: Iterable[rTriple[Rdf]]): rGraph[Rdf]
		def apply(head: rTriple[Rdf], tail: rTriple[Rdf]*): rGraph[Rdf] =
			val it: Iterable[rTriple[Rdf]] = Iterable[rTriple[Rdf]](tail.prepended(head)*)
			rGraph.apply(it)
		def triplesIn(graph: rGraph[Rdf]): Iterable[rTriple[Rdf]]
		def graphSize(graph: rGraph[Rdf]): Int

//	given tripleTT: TypeTest[Matchable, Triple[Rdf]]
	val Statement: StatementOps
	trait StatementOps:
		extension (subj: St.Subject[Rdf])
			def fold[A](uriFnct: URI[Rdf] => A, bnFcnt: BNode[Rdf] => A): A

//	extension (obj: Statement.Object[Rdf])
//		def fold[A](bnFcnt: BNode[Rdf] => A, uriFnct: URI[Rdf] => A, litFnc: Literal[Rdf] => A): A =
//			obj match
//			case bn: BNode[Rdf] =>  bnFcnt(bn)
//			case n: URI[Rdf] => uriFnct(n)
//			case lit: Literal[Rdf] => litFnc(lit)

	given Triple: TripleOps
	trait TripleOps:
		def apply(s: St.Subject[Rdf], p: St.Relation[Rdf], o: St.Object[Rdf]): Triple[Rdf]
		def unapply(t: Triple[Rdf]): Option[TripleI] = Some(untuple(t))
		def untuple(t: Triple[Rdf]): TripleI
		protected def subjectOf(s: Triple[Rdf]): St.Subject[Rdf]
		protected def relationOf(s: Triple[Rdf]): St.Relation[Rdf]
		protected def objectOf(s: Triple[Rdf]): St.Object[Rdf]
		extension (triple: Triple[Rdf])
			def subj: St.Subject[Rdf] = subjectOf(triple)
			def rel: St.Relation[Rdf] = relationOf(triple)
			def obj: St.Object[Rdf] = objectOf(triple)

	val rTriple: rTripleOps
	trait rTripleOps:
		import RDF.rStatement as rSt
		def apply(s: rSt.Subject[Rdf], p: rSt.Relation[Rdf], o: rSt.Object[Rdf]): rTriple[Rdf]
		def unapply(t: RDF.Triple[Rdf]): Option[rTripleI] = Some(untuple(t))
		def untuple(t: RDF.Triple[Rdf]): rTripleI
		protected
		def subjectOf(s: rTriple[Rdf]): rSt.Subject[Rdf]
		protected
		def relationOf(s: rTriple[Rdf]): rSt.Relation[Rdf]
		protected
		def objectOf(s: rTriple[Rdf]): rSt.Object[Rdf]
		//todo? should we only have the extension functions?
		extension (rtriple: rTriple[Rdf])
			def rsubj: rSt.Subject[Rdf] = subjectOf(rtriple)
			def rrel: rSt.Relation[Rdf]   = relationOf(rtriple)
			def robj: rSt.Object[Rdf]  = objectOf(rtriple)
	end rTripleOps

	given Node: NodeOps
	trait NodeOps:
		extension (node: Node[Rdf])
			def fold[A](
				uriF: URI[Rdf] => A,
				bnF:  BNode[Rdf] => A,
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
		@targetName("langLit")
		def apply(lex: String, lang: Lang[Rdf]): Literal[Rdf]
		@targetName("dataTypeLit")
		def apply(lex: String, dataTp: URI[Rdf]): Literal[Rdf]

		lazy val langTp: URI[Rdf] = URI(xsdLangStr)
		lazy val stringTp: URI[Rdf] = URI(xsdStr)

		extension (lit: Literal[Rdf])
			def text: String
			//todo: this can be implemented more efficiently in individual subclasses by
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
			def lang: Option[Lang[Rdf]] = lit.fold(_=>None, (_,l) => Some(l), (_,_) => None)
			def dataType: URI[Rdf] = lit.fold(_=>stringTp,(_,_)=>langTp,(_,tp)=>tp)

		extension (str: String)
			@targetName("dt")
			infix def ^^(dtType: URI[Rdf]): Literal[Rdf] =
				apply(str, dtType)
			@targetName("lang")
			infix def `@`(lang: Lang[Rdf]): Literal[Rdf] =
				apply(str, lang)
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
		protected def asString(uri: URI[Rdf]): String
		extension (uri: URI[Rdf])
			def value: String = asString(uri)
			def ===(other: URI[Rdf]): Boolean = uri.equals(other)


	given Lang: LangOps
	trait LangOps:
		def apply(name: String): Lang[Rdf]
		def unapply(lang: Lang[Rdf]): Option[String] = Some(lang.label)
		extension (lang: RDF.Lang[Rdf])
			def label: String

end Ops

