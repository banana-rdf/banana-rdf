package org.w3.rdf.jena.util

import org.w3.rdf._
import org.w3.rdf.jena._

/**
 * a Jena based default GraphIsomorphism
 *
 * The given graph is transformed into the Jena world using a Transformer
 */
class JenaBasedGraphIsomorphism[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends GraphIsomorphism[Rdf] {
  
  private val mToJena = new RDFTransformer[Rdf, Jena](ops, JenaOperations)
  private val jenaToM = new RDFTransformer[Jena, Rdf](JenaOperations, ops)
  
  def isomorphism(g1: Rdf#Graph, g2: Rdf#Graph): Boolean = {
    val j1 = mToJena.transform(g1)
    val j2 = mToJena.transform(g2)
    JenaGraphIsomorphism.isomorphism(j1, j2)
  }
  
}

object JenaBasedGraphIsomorphism {
  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): JenaBasedGraphIsomorphism[Rdf] =
    new JenaBasedGraphIsomorphism[Rdf](ops)
}
