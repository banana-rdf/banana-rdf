package org.w3.banana

import scalaz._
import Id._

abstract class SparqlEngineTest[Rdf <: RDF](val store: RDFStore[Rdf])(
  implicit diesel: Diesel[Rdf],
  reader: BlockingReader[Rdf#Graph, RDFXML],
  sparqlOps: SPARQLOperations[Rdf])
    extends MSparqlEngineTest[Rdf, Id]
