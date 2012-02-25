package org.w3.rdf

trait GraphIsomorphism[Rdf <: RDF] {
  
  def isomorphism(left: Rdf#Graph, right: Rdf#Graph): Boolean
  
  class GraphIsomorphismW(graph: Rdf#Graph) {
    def isIsomorphicWith(otherGraph: Rdf#Graph): Boolean = isomorphism(graph, otherGraph)
  }
  
  implicit def wrapGraphForIsomorphism(graph: Rdf#Graph): GraphIsomorphismW = new GraphIsomorphismW(graph)
  
}
