package org.w3.banana

case class PointedGraph[Rdf <: RDF](pointer: Rdf#Node, graph: Rdf#Graph) {

  def resolveAgainst(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): PointedGraph[Rdf] = {
    import diesel._
    PointedGraph[Rdf](
      pointer.resolveAgainst(baseUri),
      graph.resolveAgainst(baseUri))
  }

  def relativize(baseUri: Rdf#URI)(implicit diesel: Diesel[Rdf]): PointedGraph[Rdf] = {
    import diesel._
    PointedGraph[Rdf](
      pointer.relativize(baseUri),
      graph.relativize(baseUri))
  }

}

object PointedGraph {

  def apply[Rdf <: RDF](node: Rdf#Node)(implicit ops: RDFOperations[Rdf]): PointedGraph[Rdf] =
    new PointedGraph[Rdf](node, ops.emptyGraph)

}
