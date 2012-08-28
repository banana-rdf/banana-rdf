package org.w3.banana

import scalaz._
import Id._

abstract class GraphStoreTest[Rdf <: RDF](
  val store: GraphStore[Rdf])(
    implicit diesel: Diesel[Rdf],
    reader: RDFReader[Rdf, RDFXML])
    extends MGraphStoreTest[Rdf, Id]
