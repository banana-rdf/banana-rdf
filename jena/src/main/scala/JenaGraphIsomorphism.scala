package org.w3.rdf.jena

import org.w3.rdf

object JenaGraphIsomorphism extends rdf.GraphIsomorphism[JenaModule.type](JenaModule) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean =
    g1.jenaGraph isIsomorphicWith g2.jenaGraph
  
}