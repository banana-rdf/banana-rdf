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

  def apply[Rdf <: RDF, Sparql <: SPARQL](store: RDFStore[Rdf, Sparql], system: ActorSystem)(implicit timeout: Timeout): AsyncRDFStore[Rdf, Sparql] =
    new AsyncRDFStoreBase[Rdf, Sparql](store, system)(timeout)

  val DEFAULT_CONFIG = com.typesafe.config.ConfigFactory.parseString("""
akka.actor.deployment {
  /rdfstore {
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

}
