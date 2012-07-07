package org.w3.banana

// a monadic RDF store
trait MRDFStore[Rdf <: RDF, M[_]]
  extends MGraphStore[Rdf, M] with MSPARQLEngine[Rdf, M]
