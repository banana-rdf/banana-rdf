package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

/**
 * TODO
 */
trait AsyncSPARQLEngine[Rdf <: RDF] {

  def executeSelect(query: Rdf#SelectQuery): Future[Rdf#Solutions]

  def executeConstruct(query: Rdf#ConstructQuery): Future[Rdf#Graph]

  def executeAsk(query: Rdf#AskQuery): Future[Boolean]

}
