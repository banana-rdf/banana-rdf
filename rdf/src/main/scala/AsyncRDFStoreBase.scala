package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

class AsyncRDFStoreBase[Rdf <: RDF, Sparql <: SPARQL](
  store: RDFStore[Rdf, Sparql],
  factory: ActorRefFactory)(
    implicit timeout: Timeout)
    extends AsyncRDFStore[Rdf, Sparql] {

  case class AddNamedGraph(uri: Rdf#URI, graph: Rdf#Graph)
  case class AppendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph)
  case class GetNamedGraph(uri: Rdf#URI)
  case class RemoveGraph(uri: Rdf#URI)
  case class Select(query: Sparql#SelectQuery)
  case class Construct(query: Sparql#ConstructQuery)
  case class Ask(query: Sparql#AskQuery)

  class RDFStoreActor(store: RDFStore[Rdf, Sparql]) extends Actor {

    def receive = {
      case AddNamedGraph(uri, graph) => {
        store.addNamedGraph(uri, graph)
        sender ! ()
      }
      case AppendToNamedGraph(uri, graph) => {
        store.appendToNamedGraph(uri, graph)
        sender ! ()
      }
      case GetNamedGraph(uri) => {
        val graph = store.getNamedGraph(uri)
        sender ! graph
      }
      case RemoveGraph(uri) => {
        store.removeGraph(uri)
        sender ! ()
      }
      case Select(query) => {
        val rows = store.executeSelect(query)
        sender ! rows
      }
      case Construct(query) => {
        val graph = store.executeConstruct(query)
        sender ! graph
      }
      case Ask(query) => {
        val b = store.executeAsk(query)
        sender ! b
      }
    }

  }

  val storeActor =
    factory.actorOf(
      Props(new RDFStoreActor(store))
        .withRouter(FromConfig())
        .withDispatcher("rdfstore-dispatcher"),
      "rdfstore")

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit] =
    storeActor.?(AddNamedGraph(uri, graph)).mapTo[Unit]

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit] =
    storeActor.?(AppendToNamedGraph(uri, graph)).mapTo[Unit]

  def getNamedGraph(uri: Rdf#URI): Future[Rdf#Graph] =
    storeActor.?(GetNamedGraph(uri)).asInstanceOf[Future[Rdf#Graph]]

  def removeGraph(uri: Rdf#URI): Future[Unit] =
    storeActor.?(RemoveGraph(uri)).mapTo[Unit]

  def executeSelect(query: Sparql#SelectQuery): Future[Sparql#Solutions] =
    storeActor.?(Select(query)).asInstanceOf[Future[Sparql#Solutions]]

  def executeConstruct(query: Sparql#ConstructQuery): Future[Rdf#Graph] =
    storeActor.?(Construct(query)).asInstanceOf[Future[Rdf#Graph]]

  def executeAsk(query: Sparql#AskQuery): Future[Boolean] =
    storeActor.?(Ask(query)).asInstanceOf[Future[Boolean]]

}
