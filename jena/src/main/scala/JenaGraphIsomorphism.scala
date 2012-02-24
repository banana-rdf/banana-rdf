package org.w3.rdf.jena

import org.w3.rdf._

object JenaGraphIsomorphism extends GraphIsomorphism[JenaDataType] {
  
  def isomorphism(left: JenaDataType#Graph, right: JenaDataType#Graph): Boolean =
    left isIsomorphicWith right
  
}