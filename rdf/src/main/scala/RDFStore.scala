package org.w3.banana

trait RDFStore[Rdf <: RDF]
  extends GraphStore[Rdf] with SPARQLEngine[Rdf]
