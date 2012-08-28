package org.w3.banana

import util._
import scalaz._
import Id._

trait RDFStore[Rdf <: RDF] extends MRDFStore[Rdf, Id]
  with GraphStore[Rdf] with SPARQLEngine[Rdf]

trait AsyncRDFStore[Rdf <: RDF] extends MRDFStore[Rdf, BananaFuture]
  with AsyncGraphStore[Rdf] with AsyncSPARQLEngine[Rdf]

object AsyncRDFStore {

  import akka.actor.ActorSystem
  import akka.util.Timeout

  def apply[Rdf <: RDF](store: RDFStore[Rdf], system: ActorSystem)(implicit timeout: Timeout): AsyncRDFStore[Rdf] =
    new AsyncRDFStoreBase[Rdf](store, system)(timeout)

}

trait MRDFStore[Rdf <: RDF, M[_]]
  extends MGraphStore[Rdf, M] with MSPARQLEngine[Rdf, M]

