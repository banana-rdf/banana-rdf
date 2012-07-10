package org.w3.banana

trait RDFStore[Rdf <: RDF] extends MRDFStore[Rdf, scalaz.Id]
  // !important for type hierarchy
  with GraphStore[Rdf] with SPARQLEngine[Rdf]
