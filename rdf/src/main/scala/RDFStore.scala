package org.w3.banana

/**
 * to manipulate named graph within a Store
 */
trait RDFStore[Rdf <: RDF, Sparql <: SPARQL]
extends GraphStore[Rdf]
with SPARQLEngine[Rdf, Sparql]
