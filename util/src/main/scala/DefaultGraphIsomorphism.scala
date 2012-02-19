package org.w3.rdf.util

import org.w3.rdf
import org.w3.rdf.{RDFModule, Transformer}
import org.w3.rdf.jena
import org.w3.rdf.jena.{JenaModule}

class DefaultGraphIsomorphism[M <: RDFModule](override val m: M) extends rdf.GraphIsomorphism(m) {
  
  private val mToJena = new Transformer[m.type, JenaModule.type](m, JenaModule)
  private val jenaToM = new Transformer[JenaModule.type, m.type](JenaModule, m)
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    jena.GraphIsomorphism.isIsomorphicWith(j1, j2)
  }
  
  def diff(g1: m.Graph, g2: m.Graph): m.Graph = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    val d = jena.GraphIsomorphism.diff(j1, j2)
    jenaToM.transform(d)
  }

  
}