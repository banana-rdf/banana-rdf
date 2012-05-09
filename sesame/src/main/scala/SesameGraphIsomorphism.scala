package org.w3.banana.sesame

import org.w3.banana._

import org.openrdf.model.Statement
import org.openrdf.model.impl.GraphImpl
import org.openrdf.model.util.ModelUtil

object SesameGraphIsomorphism extends GraphIsomorphism[Sesame] {
  
  def isomorphism(left: Sesame#Graph, right: Sesame#Graph): Boolean =
    ModelUtil.equals(left, right)
  
}