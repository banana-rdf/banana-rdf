package org.w3.banana

trait RDFStore[Rdf <: RDF, Sparql <: SPARQL]
  extends GraphStore[Rdf] with SPARQLEngine[Rdf, Sparql]
