package org.w3.rdf.jena

import org.w3.rdf._

object JenaGraphIsomorphism extends GraphIsomorphism[Jena] {
  
  def isomorphism(left: Jena#Graph, right: Jena#Graph): Boolean =
    left isIsomorphicWith right
  
}