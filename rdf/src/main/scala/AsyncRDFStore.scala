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

  def apply[Rdf <: RDF, Sparql <: SPARQL](
      store: RDFStore[Rdf, Sparql],
      system: ActorSystem)(
      implicit timeout: Timeout): AsyncRDFStore[Rdf, Sparql] =
    new AsyncRDFStore[Rdf, Sparql] with AsyncGraphStoreBase[Rdf] with AsyncSPARQLEngineBase[Rdf, Sparql] {
      val graphStore = store
      val sparqlEngine = store
      val factory = system
      implicit val futuresTimeout = timeout
    }
  
}
