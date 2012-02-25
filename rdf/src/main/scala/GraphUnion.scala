package org.w3.rdf

trait GraphUnion[Rdf <: RDF] { self =>
  
  def union(left: Rdf#Graph, right: Rdf#Graph): Rdf#Graph
  
  class GraphUnionW(graph: Rdf#Graph) {
    def union(otherGraph: Rdf#Graph): Rdf#Graph = self.union(graph, otherGraph)
  }
  
  implicit def wrapGraphInGraphUnionW(graph: Rdf#Graph): GraphUnionW = new GraphUnionW(graph)
  
}
