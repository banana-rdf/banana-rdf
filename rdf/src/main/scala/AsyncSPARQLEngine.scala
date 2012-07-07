package org.w3.banana

import akka.dispatch._

/**
 * TODO
 */
trait AsyncSPARQLEngine[Rdf <: RDF] extends MSPARQLEngine[Rdf, Future]
