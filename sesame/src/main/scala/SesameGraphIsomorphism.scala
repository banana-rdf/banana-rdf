package org.w3.rdf.sesame

import org.w3.rdf._

import org.openrdf.model.Statement
import org.openrdf.model.impl.GraphImpl
import org.openrdf.model.util.ModelUtil

object SesameGraphIsomorphism extends GraphIsomorphism[SesameDataType] {
  
  def isomorphism(left: SesameDataType#Graph, right: SesameDataType#Graph): Boolean =
    ModelUtil.equals(left, right)
  
}