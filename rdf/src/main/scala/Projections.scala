package org.w3.rdf

trait Projections[Rdf <: RDF] { self =>

  def getObjects(graph: Rdf#Graph, subject: Rdf#Node, predicate: Rdf#IRI): Iterable[Rdf#Node]

  class GraphW(graph: Rdf#Graph) {
    def getObjects(subject: Rdf#Node, predicate: Rdf#IRI): Iterable[Rdf#Node] =
      self.getObjects(graph, subject, predicate)
  }

  implicit def decorateGraphWithProjections(graph: Rdf#Graph) = new GraphW(graph)

}
