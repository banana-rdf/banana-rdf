//package org.w3.banana

//import org.w3.banana.RDF
//
//
//trait RDFOps2[T <: RDFObj](using val rdf: T) {
//  def emptyGraph: rdf.Graph
//  def fromUri(uri: rdf.URI): String
//  def makeUri(s: String): rdf.URI
//}
//
//trait PointedGraph2[Rdf <: RDFObj] {
//  def pointer: Rdf#Node
//  def graph: Rdf#Graph
//}
//import RDF.*
//
//trait PG[Rdf <: RDF](val pointer: Node[Rdf], val graph: Graph[Rdf])
//
//object PG:
//	def apply[Rdf <: RDF](_pointer: Node[Rdf], _graph: Graph[Rdf]): PG[Rdf] =
//		new PG[Rdf] {
//			val pointer = _pointer
//			val graph = _graph
//		}
//
//	def apply[Rdf <: RDF](node: Node[Rdf])(using rdf: Rdf): PG[Rdf] =
//		PG(node, rdf.Graph.empty)
//
//	def unapply[Rdf <: RDF](pg: PG[Rdf]): Option[(Node[Rdf], Graph[Rdf])] =
//		Some((pg.pointer, pg.graph))
