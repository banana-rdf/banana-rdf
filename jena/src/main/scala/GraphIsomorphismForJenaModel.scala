package org.w3.rdf

import org.w3.rdf.jena._

object GraphIsomorphismForJenaModel extends GraphIsomorphism[JenaModel.type](JenaModel) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean =
    g1.jenaGraph isIsomorphicWith g2.jenaGraph
  
  
}