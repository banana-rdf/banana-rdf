package org.w3.banana

import akka.dispatch.Future
import org.scalatest.BeforeAndAfterAll
import akka.actor.ActorSystem
import akka.util.Timeout

import org.w3.banana.util._


abstract class AsyncGraphStoreTest[Rdf <: RDF](rdfStore: RDFStore[Rdf])(
  implicit diesel: Diesel[Rdf],
  reader: BlockingReader[Rdf#Graph, RDFXML])
  extends MGraphStoreTest[Rdf, Future] with BeforeAndAfterAll {

  import diesel._
  import ops._

  val system = ActorSystem("jena-asyncstore-test", util.AkkaDefaults.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)
  val store = AsyncRDFStore(rdfStore, system)

  override def afterAll(): Unit = {
    system.shutdown()
  }

}

