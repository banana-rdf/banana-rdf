package org.w3.rdf

abstract class GraphIsomorphism[M <: RDFModule](val m: M) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean
  
}
