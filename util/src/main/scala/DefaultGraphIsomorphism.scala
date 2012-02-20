package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._

class DefaultGraphIsomorphism[M <: RDFModule](override val m: M) extends GraphIsomorphism(m) {
  
  private val mToJena = new Transformer[m.type, JenaModule.type](m, JenaModule)
  private val jenaToM = new Transformer[JenaModule.type, m.type](JenaModule, m)
  
  def isIsomorphicWith(g1: m.Graph, g2: m.Graph): Boolean = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    JenaGraphIsomorphism.isIsomorphicWith(j1, j2)
  }
  
}