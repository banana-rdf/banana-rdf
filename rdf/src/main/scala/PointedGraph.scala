package org.w3.banana

trait PointedGraph[Rdf <: RDF] {

  def pointer: Rdf#Node

  def graph: Rdf#Graph

}

object PointedGraph {

  def apply[Rdf <: RDF](_pointer: Rdf#Node, _graph: Rdf#Graph): PointedGraph[Rdf] =
    new PointedGraph[Rdf] {
      val pointer = _pointer
      val graph = _graph
    }

  def apply[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOps[Rdf]): PointedGraph[Rdf] =
    PointedGraph[Rdf](node, ops.emptyGraph)

  def unapply[Rdf <: RDF](pg: PointedGraph[Rdf]): Option[(Rdf#Node,Rdf#Graph)] = Some((pg.pointer,pg.graph))

}
