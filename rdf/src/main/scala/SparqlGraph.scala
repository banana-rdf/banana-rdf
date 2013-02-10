package org.w3.banana

import scalaz.Id._

trait SparqlGraph[Rdf <: RDF] {

  def apply(graph: Rdf#Graph): SparqlEngine[Rdf, Id]

}
