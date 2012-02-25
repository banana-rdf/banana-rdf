package org.w3.rdf

trait GraphUnion[RDF <: RDFDataType] { self =>
  
  def union(left: RDF#Graph, right: RDF#Graph): RDF#Graph
  
  class GraphUnionW(graph: RDF#Graph) {
    def union(otherGraph: RDF#Graph): RDF#Graph = self.union(graph, otherGraph)
  }
  
  implicit def wrapGraphInGraphUnionW(graph: RDF#Graph): GraphUnionW = new GraphUnionW(graph)
  
}
