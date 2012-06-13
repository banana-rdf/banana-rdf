package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

trait AsyncRDFStore[Rdf <: RDF, Sparql <: SPARQL]
extends AsyncGraphStore[Rdf]
with AsyncSPARQLEngine[Rdf, Sparql]

object AsyncRDFStore {

  val DEFAULT_CONFIG = com.typesafe.config.ConfigFactory.parseString("""
akka.actor.deployment {
  /graph-store {
    router = round-robin
    nr-of-instances = 1
  }
  /sparql-engine {
    router = round-robin
    nr-of-instances = 1
  }
}

rdfstore-dispatcher {
  executor = "fork-join-executor"
  type = "Dispatcher"
  # this makes sure that there is only one actor at a time
  fork-join-executor {
    # Min number of threads to cap factor-based parallelism number to
    parallelism-min = 1
    # Parallelism (threads) ... ceil(available processors * factor)
    parallelism-factor = 3.0
    # Max number of threads to cap factor-based parallelism number to
    parallelism-max = 1
  }
}
""")

  def apply[Rdf <: RDF, Sparql <: SPARQL](
      store: RDFStore[Rdf, Sparql],
      system: ActorSystem)(
      implicit timeout: Timeout): AsyncRDFStore[Rdf, Sparql] =
    new AsyncRDFStore[Rdf, Sparql] with AsyncGraphStore[Rdf] with AsyncSPARQLEngine[Rdf, Sparql] {

      val asyncGraphStore = new AsyncGraphStoreBase[Rdf](store, system)(timeout)

      def addNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit] =
        asyncGraphStore.addNamedGraph(uri, graph)

      def appendToNamedGraph(uri: Rdf#URI, graph: Rdf#Graph): Future[Unit] =
        asyncGraphStore.appendToNamedGraph(uri, graph)

      def getNamedGraph(uri: Rdf#URI): Future[Rdf#Graph] =
        asyncGraphStore.getNamedGraph(uri)

      def removeGraph(uri: Rdf#URI): Future[Unit] =
        asyncGraphStore.removeGraph(uri)

      val asyncSparqlEngine = new AsyncSPARQLEngineBase(store, system)(timeout)

      def executeSelect(query: Sparql#SelectQuery): Future[Iterable[Sparql#Row]] =
        asyncSparqlEngine.executeSelect(query)

      def executeConstruct(query: Sparql#ConstructQuery): Future[Rdf#Graph] =
        asyncSparqlEngine.executeConstruct(query)

      def executeAsk(query: Sparql#AskQuery): Future[Boolean] =
        asyncSparqlEngine.executeAsk(query)

    }
  
}
