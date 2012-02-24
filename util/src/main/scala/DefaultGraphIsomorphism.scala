package org.w3.rdf.util

import org.w3.rdf._
import org.w3.rdf.jena._

class DefaultGraphIsomorphism[RDF <: RDFDataType](val ops: RDFOperations[RDF]) extends GraphIsomorphism[RDF] {
  
  private val mToJena = new Transformer[RDF, JenaDataType](ops, JenaOperations)
  private val jenaToM = new Transformer[JenaDataType, RDF](JenaOperations, ops)
  
  def isomorphism(g1: RDF#Graph, g2: RDF#Graph): Boolean = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    JenaGraphIsomorphism.isomorphism(j1, j2)
  }
  
}