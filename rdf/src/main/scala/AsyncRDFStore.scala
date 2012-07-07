package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout

trait AsyncRDFStore[Rdf <: RDF] extends MRDFStore[Rdf, Future]

object AsyncRDFStore {

  def apply[Rdf <: RDF](store: RDFStore[Rdf], system: ActorSystem)(implicit timeout: Timeout): AsyncRDFStore[Rdf] =
    new AsyncRDFStoreBase[Rdf](store, system)(timeout)

}
