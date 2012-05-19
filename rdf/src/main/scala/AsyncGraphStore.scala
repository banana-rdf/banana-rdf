package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

trait AsyncGraphStore[Rdf <: RDF] {

  def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit]

  def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit]

  def getNamedGraph(uri: Rdf#URI): Future[Rdf#Graph]

  def removeGraph(uri: Rdf#URI): Future[Unit]

}

trait AsyncGraphStoreBase[Rdf <: RDF] extends AsyncGraphStore[Rdf] {

  def graphStore: GraphStore[Rdf]
  def factory: ActorRefFactory
  implicit def futuresTimeout: Timeout

  case class AddNamedGraph(uri: Rdf#URI, graph: Rdf#Graph)
  case class AppendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph)
  case class GetNamedGraph(uri: Rdf#URI)
  case class RemoveGraph(uri: Rdf#URI)

  class RDFStoreActor(store: GraphStore[Rdf]) extends Actor {
    
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
    }

  }

  lazy val storeActor =
    factory.actorOf(
      Props(new RDFStoreActor(graphStore))
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

}

object AsyncGraphStore {

  def apply[Rdf <: RDF](
      store: GraphStore[Rdf],
      system: ActorSystem)(
      implicit timeout: Timeout): AsyncGraphStore[Rdf] =
    new AsyncGraphStoreBase[Rdf] {
      val graphStore = store
      val factory = system
      implicit val futuresTimeout = timeout
    }
  
}
