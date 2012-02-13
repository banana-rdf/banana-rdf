package org.w3.rdf

abstract class GraphIsomorphism[M <: Module](val m: M) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean
  
  def diff(g1: m.Graph, g2: m.Graph): m.Graph

}
