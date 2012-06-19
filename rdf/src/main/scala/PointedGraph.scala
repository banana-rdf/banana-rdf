package org.w3.banana

case class PointedGraph[Rdf <: RDF](node: Rdf#Node, graph: Rdf#Graph)

object PointedGraph {

  def apply[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOperations[Rdf]): PointedGraph[Rdf] =
    new PointedGraph[Rdf](node, ops.Graph.empty)

}
