package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.dispatch._
import akka.util.duration._
import org.w3.banana.util._

abstract class AsyncSparqlEngineTest[Rdf <: RDF](
  backendStore: RDFStore[Rdf])(
    implicit reader: BlockingReader[Rdf#Graph, RDFXML],
    diesel: Diesel[Rdf],
    sparqlOps: SPARQLOperations[Rdf])
    extends MSparqlEngineTest[Rdf, BananaFuture] with BeforeAndAfterAll {

  import diesel._
  import ops._
  import sparqlOps._

  val system = ActorSystem("jena-asynsparqlquery-test", util.AkkaDefaults.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)

  val asyncEngine = AsyncRDFStore(backendStore, system)

  def store = asyncEngine


  override def beforeAll(): Unit = {
    super.beforeAll()

  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }




}
