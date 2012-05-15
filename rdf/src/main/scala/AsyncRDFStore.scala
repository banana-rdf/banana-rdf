package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

trait AsyncRDFStore[Rdf <: RDF] {

  def addNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit]

  def appendToNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit]

  def getNamedGraph(uri: Rdf#IRI): Future[Rdf#Graph]

  def removeGraph(uri: Rdf#IRI): Future[Unit]

}

class AsyncRDFStoreBuilder[Rdf <: RDF](
  store: RDFStore[Rdf],
  factory: ActorRefFactory)
  (implicit timeout: Timeout)
extends AsyncRDFStore[Rdf] {

  case class AddNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph)
  case class AppendToNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph)
  case class GetNamedGraph(uri: Rdf#IRI)
  case class RemoveGraph(uri: Rdf#IRI)

  class RDFStoreActor(store: RDFStore[Rdf]) extends Actor {
    
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

  val storeActor =
    factory.actorOf(
      Props(new RDFStoreActor(store))
        .withRouter(FromConfig())
        .withDispatcher("rdfstore-dispatcher"),
      "rdfstore")

  def addNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit] =
    storeActor.?(AddNamedGraph(uri, graph)).mapTo[Unit]

  def appendToNamedGraph(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit] =
    storeActor.?(AppendToNamedGraph(uri, graph)).mapTo[Unit]

  def getNamedGraph(uri: Rdf#IRI): Future[Rdf#Graph] =
    storeActor.?(GetNamedGraph(uri)).asInstanceOf[Future[Rdf#Graph]]

  def removeGraph(uri: Rdf#IRI): Future[Unit] =
    storeActor.?(RemoveGraph(uri)).mapTo[Unit]

}

object AsyncRDFStore {

  val DEFAULT_CONFIG = com.typesafe.config.ConfigFactory.parseString("""
akka.actor.deployment {
  /rdfstore {
    router = round-robin
    nr-of-instances = 2
  }
}

rdfstore-dispatcher {
  executor = "thread-pool-executor"
  type = BalancingDispatcher
}
""")

  def apply[Rdf <: RDF](store: RDFStore[Rdf], system: ActorSystem)(implicit timeout: Timeout): AsyncRDFStore[Rdf] = new AsyncRDFStoreBuilder(store, system)
  
}
