package org.w3.rdf.sesame

import org.w3.rdf

import org.openrdf.model.Statement
import org.openrdf.model.impl.GraphImpl
import org.openrdf.model.util.ModelUtil
import org.openrdf.repository.util.RepositoryUtil

object SesameGraphIsomorphism extends rdf.GraphIsomorphism[SesameModule.type](SesameModule) {
  
  def isomorphism(left: m.Graph, right: m.Graph): Boolean = ModelUtil.equals(left, right)
  
}