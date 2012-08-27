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
  case class PatchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph)
  case class GetGraph(uri: Rdf#URI)
  case class RemoveGraph(uri: Rdf#URI)
  case class Select(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node])
  case class Construct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node])
  case class Ask(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node])

  class RDFStoreActor(store: RDFStore[Rdf]) extends Actor {

    def receive = {
      case AppendToGraph(uri, graph) => {
        val r = bananaCatch { store.appendToGraph(uri, graph) }
        sender ! r
      }
      case PatchGraph(uri, delete, insert) => {
        val r = bananaCatch { store.patchGraph(uri, delete, insert) }
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
      case Select(query, bindings) => {
        val rows = bananaCatch { store.executeSelect(query, bindings) }
        sender ! rows
      }
      case Construct(query, bindings) => {
        val graph = bananaCatch { store.executeConstruct(query, bindings) }
        sender ! graph
      }
      case Ask(query, bindings) => {
        val b = bananaCatch { store.executeAsk(query, bindings) }
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

  def patchGraph(uri: Rdf#URI, delete: Iterable[TripleMatch[Rdf]], insert: Rdf#Graph): BananaFuture[Unit] =
    storeActor.?(PatchGraph(uri, delete, insert)).asInstanceOf[Future[Validation[BananaException, Unit]]].fv

  def getGraph(uri: Rdf#URI): BananaFuture[Rdf#Graph] =
    storeActor.?(GetGraph(uri)).asInstanceOf[Future[Validation[BananaException, Rdf#Graph]]].fv

  def removeGraph(uri: Rdf#URI): BananaFuture[Unit] =
    storeActor.?(RemoveGraph(uri)).asInstanceOf[Future[Validation[BananaException, Unit]]].fv

  def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): BananaFuture[Rdf#Solutions] =
    storeActor.?(Select(query, bindings)).asInstanceOf[Future[Validation[BananaException, Rdf#Solutions]]].fv

  def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): BananaFuture[Rdf#Graph] =
    storeActor.?(Construct(query, bindings)).asInstanceOf[Future[Validation[BananaException, Rdf#Graph]]].fv

  def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): BananaFuture[Boolean] =
    storeActor.?(Ask(query, bindings)).asInstanceOf[Future[Validation[BananaException, Boolean]]].fv

}
