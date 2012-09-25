package org.w3.banana

import scalaz.Id._

trait RDFGraphQuery[Rdf <: RDF] {

  def makeSparqlEngine(graph: Rdf#Graph): SparqlEngine[Rdf, Id]

}
