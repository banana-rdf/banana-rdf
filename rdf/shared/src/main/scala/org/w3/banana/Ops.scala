package org.w3.banana

import org.w3.banana.RDF.Graph
import org.w3.banana.RDF.Statement.Subject

import scala.annotation.targetName
import scala.reflect.TypeTest
import scala.util.Try

trait Ops[Rdf <: RDF]:
	import scala.language.implicitConversions
	import RDF.*
	import RDF.Statement as St

	// needed to help inferencing
	// todo: this transformation should really be automatically handled by compiler. Report back.
	implicit def lit2Node(lit: Literal[Rdf]): Node[Rdf] = lit.asInstanceOf[Node[Rdf]]
	implicit def uri2Node(uri: URI[Rdf]): Node[Rdf] = uri.asInstanceOf[Node[Rdf]]
	implicit def bnode2Node(bn: BNode[Rdf]): Node[Rdf] = bn.asInstanceOf[Node[Rdf]]
	implicit def uri2rUri(uri: URI[Rdf]): rURI[Rdf] = uri.asInstanceOf[rURI[Rdf]]
	implicit def rUri2rNode(uri: rURI[Rdf]): rNode[Rdf] = uri.asInstanceOf[rNode[Rdf]]

	//conversions for position types
	implicit def obj2Node(obj: St.Object[Rdf]): Node[Rdf] = obj.asInstanceOf[Node[Rdf]]
	implicit def sub2Node(obj: St.Subject[Rdf]): Node[Rdf] = obj.asInstanceOf[Node[Rdf]]
	//note:  if we use the conversion below, then all the code needs to import scala.language.implicitConversions
	//	given Conversion[St.Object[Rdf],RDF.Node[Rdf]] with
	//		def apply(obj: St.Object[Rdf]): RDF.Node[Rdf] =  obj.asInstanceOf[Node[Rdf]]

	// interpretation types to help consistent pattern matching across implementations

	type QuadI = (St.Subject[Rdf], St.Relation[Rdf], St.Object[Rdf], St.Graph[Rdf])
	//implementations
	val `*`: NodeAny[Rdf]

	given Graph: operations.Graph[Rdf]

	val rGraph: operations.rGraph[Rdf]

//	given tripleTT: TypeTest[Matchable, Triple[Rdf]]
	val Statement: operations.Statement[Rdf]

//	extension (obj: Statement.Object[Rdf])
//		def fold[A](bnFcnt: BNode[Rdf] => A, uriFnct: URI[Rdf] => A, litFnc: Literal[Rdf] => A): A =
//			obj match
//			case bn: BNode[Rdf] =>  bnFcnt(bn)
//			case n: URI[Rdf] => uriFnct(n)
//			case lit: Literal[Rdf] => litFnc(lit)

	given Triple: operations.Triple[Rdf]

	//	given Quad: QuadOps
//	trait QuadOps:
//		def apply(s: St.Subject[Rdf], p: St.Relation[Rdf], o: St.Object[Rdf]): Quad[Rdf]
//		def apply(
//			s: St.Subject[Rdf], p: St.Relation[Rdf],
//			o: St.Object[Rdf], where: St.Graph[Rdf]
//		): Quad[Rdf]
//		def unapply(t: Quad[Rdf]): Option[QuadI] = Some(untuple(t))
//		def untuple(t: Quad[Rdf]): QuadI
//		protected def subjectOf(s: Quad[Rdf]): St.Subject[Rdf]
//		protected def relationOf(s: Quad[Rdf]): St.Relation[Rdf]
//		protected def objectOf(s: Quad[Rdf]): St.Object[Rdf]
//		protected def graphOf(s: Quad[Rdf]): St.Graph[Rdf]
//		extension (quad: Quad[Rdf])
//			def subj: St.Subject[Rdf] = subjectOf(quad)
//			def rel: St.Relation[Rdf] = relationOf(quad)
//			def obj: St.Object[Rdf] = objectOf(quad)
//			def graph: St.Object[Rdf] = graphOf(quad)

	val rTriple: operations.rTriple[Rdf]

	given Node: operations.Node[Rdf]

	//todo? should a BNode be part of a Graph (or DataSet) as per Benjamin Braatz's thesis?
	given BNode: operations.BNode[Rdf]

	val Literal: operations.Literal[Rdf]
	export Literal.LiteralI.*
	given operations.Literal[Rdf] = Literal

	given literalTT: TypeTest[Matchable, RDF.Literal[Rdf]]

	val rURI: operations.rURI[Rdf]

	given URI: operations.URI[Rdf]

	given Lang: operations.Lang[Rdf]

end Ops

