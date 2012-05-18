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

  def executeSelect(query: Sparql#SelectQuery): Future[Iterable[Sparql#Row]]

  def executeConstruct(query: Sparql#ConstructQuery): Future[Rdf#Graph]

  def executeAsk(query: Sparql#AskQuery): Future[Boolean]

}

trait AsyncSPARQLEngineBase[Rdf <: RDF, Sparql <: SPARQL] extends AsyncSPARQLEngine[Rdf, Sparql] {

  def sparqlEngine: SPARQLEngine[Rdf, Sparql]
  def factory: ActorRefFactory
  implicit def futuresTimeout: Timeout

  case class Select(query: Sparql#SelectQuery)
  case class Construct(query: Sparql#ConstructQuery)
  case class Ask(query: Sparql#AskQuery)

  class EngineActor(sparqlEngine: SPARQLEngine[Rdf, Sparql]) extends Actor {

    def receive = {
      case Select(query) => {
        val rows = sparqlEngine.executeSelect(query)
        sender ! rows
      }
      case Construct(query) => {
        val graph = sparqlEngine.executeConstruct(query)
        sender ! graph
      }
      case Ask(query) => {
        val b = sparqlEngine.executeAsk(query)
        sender ! b
      }
    }
  }

  lazy val engineActor =
    factory.actorOf(
      Props(new EngineActor(sparqlEngine))
        .withRouter(FromConfig())
        .withDispatcher("rdfstore-dispatcher"),
      "rdfstore")

  def executeSelect(query: Sparql#SelectQuery): Future[Iterable[Sparql#Row]] =
    engineActor.?(Select(query)).asInstanceOf[Future[Iterable[Sparql#Row]]]

  def executeConstruct(query: Sparql#ConstructQuery): Future[Rdf#Graph] =
    engineActor.?(Construct(query)).asInstanceOf[Future[Rdf#Graph]]

  def executeAsk(query: Sparql#AskQuery): Future[Boolean] =
    engineActor.?(Ask(query)).asInstanceOf[Future[Boolean]]

}

object AsyncSPARQLEngine {

  def apply[Rdf <: RDF, Sparql <: SPARQL](
    engine: SPARQLEngine[Rdf, Sparql],
    system: ActorSystem)(
    implicit timeout: Timeout): AsyncSPARQLEngine[Rdf, Sparql] =
      new AsyncSPARQLEngineBase[Rdf, Sparql] {
        val sparqlEngine = engine
        val factory = system
        implicit val futuresTimeout = timeout
      }

}
