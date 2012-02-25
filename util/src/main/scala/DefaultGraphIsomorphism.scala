package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._

class DefaultGraphIsomorphism[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends GraphIsomorphism[Rdf] {
  
  private val mToJena = new RDFTransformer[Rdf, JenaDataType](ops, JenaOperations)
  private val jenaToM = new RDFTransformer[JenaDataType, Rdf](JenaOperations, ops)
  
  def isomorphism(g1: Rdf#Graph, g2: Rdf#Graph): Boolean = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    JenaGraphIsomorphism.isomorphism(j1, j2)
  }
  
}