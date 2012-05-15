package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

/**
 * TODO
 */
trait AsyncRDFQuery[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelectQuery(query: Sparql#SelectQuery): Future[Iterable[Sparql#Row]]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(query: Sparql#ConstructQuery): Future[Rdf#Graph]

  def executeAskQuery(query: Sparql#AskQuery): Future[Boolean]

}

class AsyncRDFQueryBuilder[Rdf <: RDF, Sparql <: SPARQL](
  queryEngine: RDFQuery[Rdf, Sparql],
  factory: ActorRefFactory)(
  implicit timeout: Timeout)
extends AsyncRDFQuery[Rdf, Sparql] {

  case class Select(query: Sparql#SelectQuery)
  case class Construct(query: Sparql#ConstructQuery)
  case class Ask(query: Sparql#AskQuery)

  class QueryActor(queryEngine: RDFQuery[Rdf, Sparql]) extends Actor {

    def receive = {
      case Select(query) => {
        val rows = queryEngine.executeSelectQuery(query)
        sender ! rows
      }
      case Construct(query) => {
        val graph = queryEngine.executeConstructQuery(query)
        sender ! graph
      }
      case Ask(query) => {
        val b = queryEngine.executeAskQuery(query)
        sender ! b
      }
    }
  }

  val queryActor =
    factory.actorOf(
      Props(new QueryActor(queryEngine))
        .withRouter(FromConfig())
        .withDispatcher("rdfstore-dispatcher"),
      "rdfstore")

  def executeSelectQuery(query: Sparql#SelectQuery): Future[Iterable[Sparql#Row]] =
    queryActor.?(Select(query)).asInstanceOf[Future[Iterable[Sparql#Row]]]

  def executeConstructQuery(query: Sparql#ConstructQuery): Future[Rdf#Graph] =
    queryActor.?(Construct(query)).asInstanceOf[Future[Rdf#Graph]]

  def executeAskQuery(query: Sparql#AskQuery): Future[Boolean] =
    queryActor.?(Ask(query)).asInstanceOf[Future[Boolean]]

  def getNode(row: Sparql#Row, v: String): Rdf#Node = queryEngine.getNode(row, v)

}

object AsyncRDFQuery {

  def apply[Rdf <: RDF, Sparql <: SPARQL](
    queryEngine: RDFQuery[Rdf, Sparql],
    system: ActorSystem)(
    implicit timeout: Timeout): AsyncRDFQuery[Rdf, Sparql] =
      new AsyncRDFQueryBuilder(queryEngine, system)

}
