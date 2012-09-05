package org.w3.banana

import scalaz.Id._

trait RDFGraphQuery[Rdf <: RDF] {

  def makeSPARQLEngine(graph: Rdf#Graph): SPARQLEngine[Rdf, Id]

}
