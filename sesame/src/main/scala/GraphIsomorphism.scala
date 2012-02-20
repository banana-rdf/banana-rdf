package org.w3.rdf.sesame

import org.w3.rdf

import org.openrdf.model.Statement
import org.openrdf.model.impl.GraphImpl
import org.openrdf.model.util.ModelUtil
import org.openrdf.repository.util.RepositoryUtil

import java.util.Collection

object GraphIsomorphism extends rdf.GraphIsomorphism[SesameModule.type](SesameModule) {
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean =
    ModelUtil.equals(
      g1.sesameGraph.iterator.asInstanceOf[Collection[Statement]],
      g2.sesameGraph.iterator.asInstanceOf[Collection[Statement]]
    )
  
  def diff(g1: m.Graph, g2: m.Graph): m.Graph = {
    new m.Graph(new GraphImpl(
      RepositoryUtil.difference(
        g1.sesameGraph.iterator.asInstanceOf[Collection[Statement]],
        g2.sesameGraph.iterator.asInstanceOf[Collection[Statement]]
    )))
  }
  
}