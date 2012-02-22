package org.w3.rdf.jena

import org.w3.rdf

object JenaGraphIsomorphism extends rdf.GraphIsomorphism[JenaModule.type](JenaModule) {
  
  def isomorphism(left: m.Graph, right: m.Graph): Boolean = left isIsomorphicWith right
  
}