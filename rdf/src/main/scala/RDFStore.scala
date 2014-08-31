package org.w3.banana

trait RDFStore[Rdf <: RDF, T]
    extends SparqlEngine[Rdf, T]
    with GraphStore[Rdf, T]
    with Transactor[Rdf, T]
