package org.w3.banana

import akka.dispatch._
import org.w3.banana.util.BananaFuture

trait AsyncGraphStore[Rdf <: RDF] extends MGraphStore[Rdf, BananaFuture]
