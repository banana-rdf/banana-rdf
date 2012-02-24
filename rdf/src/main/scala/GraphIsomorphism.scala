package org.w3.rdf

trait GraphIsomorphism[RDF <: RDFDataType] {
  
  def isomorphism(left: RDF#Graph, right: RDF#Graph): Boolean
  
  class GraphIsomorphismW(graph: RDF#Graph) {
    def isIsomorphicWith(otherGraph: RDF#Graph): Boolean = isomorphism(graph, otherGraph)
  }
  
  implicit def wrapGraphForIsomorphism(graph: RDF#Graph): GraphIsomorphismW = new GraphIsomorphismW(graph)
  
}
