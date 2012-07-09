package org.w3.banana

import akka.dispatch._
import akka.actor._
import akka.pattern.ask
import akka.routing._
import akka.util.Timeout
import org.w3.banana.util.BananaFuture

trait AsyncRDFStore[Rdf <: RDF] extends MRDFStore[Rdf, BananaFuture]

object AsyncRDFStore {

  def apply[Rdf <: RDF](store: RDFStore[Rdf], system: ActorSystem)(implicit timeout: Timeout): AsyncRDFStore[Rdf] =
    new AsyncRDFStoreBase[Rdf](store, system)(timeout)

}
