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

}
