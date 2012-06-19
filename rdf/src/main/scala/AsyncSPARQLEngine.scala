package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

/**
 * TODO
 */
trait AsyncSPARQLEngine[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelect(query: Sparql#SelectQuery): Future[Iterable[Row[Rdf]]]

  def executeConstruct(query: Sparql#ConstructQuery): Future[Rdf#Graph]

  def executeAsk(query: Sparql#AskQuery): Future[Boolean]

}
