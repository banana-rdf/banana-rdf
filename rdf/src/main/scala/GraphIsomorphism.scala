package org.w3.rdf

abstract class GraphIsomorphism[M <: RDFModule](val m: M) {
  
  def isomorphism(left: m.Graph, right: m.Graph): Boolean
  
  class GraphIsomorphismW(graph: m.Graph) {
    def isIsomorphicWith(otherGraph: m.Graph): Boolean = isomorphism(graph, otherGraph)
  }
  
  implicit def wrapGraphForIsomorphism(graph: m.Graph): GraphIsomorphismW = new GraphIsomorphismW(graph)
  
}
