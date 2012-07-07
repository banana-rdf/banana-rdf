package org.w3.banana

import akka.dispatch._

trait AsyncGraphStore[Rdf <: RDF] extends MGraphStore[Rdf, Future]
