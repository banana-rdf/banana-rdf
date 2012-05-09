package org.w3.banana.jena

import org.w3.banana._

object JenaGraphIsomorphism extends GraphIsomorphism[Jena] {
  
  def isomorphism(left: Jena#Graph, right: Jena#Graph): Boolean =
    left isIsomorphicWith right
  
}