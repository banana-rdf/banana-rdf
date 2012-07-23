package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout
import org.w3.banana.util._
import org.w3.banana.BananaException._
import scalaz.Validation

class AsyncRDFStoreBase[Rdf <: RDF](
  store: RDFStore[Rdf],
  factory: ActorRefFactory)(
    implicit timeout: Timeout)
    extends AsyncRDFStore[Rdf] {

  case class AppendToGraph(uri: Rdf#URI, graph: Rdf#Graph)
  case class GetGraph(uri: Rdf#URI)
  case class RemoveGraph(uri: Rdf#URI)
  case class Select(query: Rdf#SelectQuery)
  case class Construct(query: Rdf#ConstructQuery)
  case class Ask(query: Rdf#AskQuery)

  class RDFStoreActor(store: RDFStore[Rdf]) extends Actor {

    def receive = {
      case AppendToGraph(uri, graph) => {
        val r = bananaCatch { store.appendToGraph(uri, graph) }
        sender ! r
      }
      case GetGraph(uri) => {
        val graph = bananaCatch { store.getGraph(uri) }
        sender ! graph
      }
      case RemoveGraph(uri) => {
        val r = bananaCatch { store.removeGraph(uri) }
        sender ! r
      }
      case Select(query) => {
        val rows = bananaCatch { store.executeSelect(query) }
        sender ! rows
      }
      case Construct(query) => {
        val graph = bananaCatch { store.executeConstruct(query) }
        sender ! graph
      }
      case Ask(query) => {
        val b = bananaCatch { store.executeAsk(query) }
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

  def appendToGraph(uri: Rdf#URI, graph: Rdf#Graph): BananaFuture[Unit] =
    storeActor.?(AppendToGraph(uri, graph)).asInstanceOf[Future[Validation[BananaException, Unit]]].fv

  def getGraph(uri: Rdf#URI): BananaFuture[Rdf#Graph] =
    storeActor.?(GetGraph(uri)).asInstanceOf[Future[Validation[BananaException, Rdf#Graph]]].fv

  def removeGraph(uri: Rdf#URI): BananaFuture[Unit] =
    storeActor.?(RemoveGraph(uri)).asInstanceOf[Future[Validation[BananaException, Unit]]].fv

  def executeSelect(query: Rdf#SelectQuery): BananaFuture[Rdf#Solutions] =
    storeActor.?(Select(query)).asInstanceOf[Future[Validation[BananaException, Rdf#Solutions]]].fv

  def executeConstruct(query: Rdf#ConstructQuery): BananaFuture[Rdf#Graph] =
    storeActor.?(Construct(query)).asInstanceOf[Future[Validation[BananaException, Rdf#Graph]]].fv

  def executeAsk(query: Rdf#AskQuery): BananaFuture[Boolean] =
    storeActor.?(Ask(query)).asInstanceOf[Future[Validation[BananaException, Boolean]]].fv

}
