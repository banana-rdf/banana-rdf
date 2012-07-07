package org.w3.banana

import scalaz.Id

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF] extends MSPARQLEngine[Rdf, Id]
