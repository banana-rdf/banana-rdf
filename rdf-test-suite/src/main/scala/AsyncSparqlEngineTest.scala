package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.dispatch._
import akka.util.duration._
import org.w3.banana.util._

abstract class AsyncSparqlEngineTest[Rdf <: RDF](
  store: RDFStore[Rdf])(
    implicit reader: BlockingReader[Rdf#Graph, RDFXML],
    diesel: Diesel[Rdf],
    sparqlOps: SPARQLOperations[Rdf])
    extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import diesel._
  import ops._
  import sparqlOps._

  val system = ActorSystem("jena-asynsparqlquery-test", util.AkkaDefaults.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)

  val asyncEngine = AsyncRDFStore(store, system)

  val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(file, "http://example.com") getOrElse sys.error("ouch")

  override def beforeAll(): Unit = {
    store.addNamedGraph(uri("http://example.com/graph"), graph)
  }

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "new-tr.rdf must have Alexandre Bertails as an editor" in {

    val query = SelectQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

SELECT DISTINCT ?name WHERE {
  graph <http://example.com/graph> {
    ?thing :editor ?ed .
    ?ed contact:fullName ?name
  }
}
""")

    val names: Iterable[String] = asyncEngine.executeSelect(query).awaitSuccess().toIterable map { row => row("name").flatMap(_.as[String]) getOrElse sys.error("") }

    names must contain("Alexandre Bertails")

  }

  "the identity SPARQL Construct must work as expected" in {

    val query = ConstructQuery("""
CONSTRUCT {
  ?s ?p ?o
} WHERE {
  graph <http://example.com/graph> {
    ?s ?p ?o
  }
}
""")

    val clonedGraph = asyncEngine.executeConstruct(query).awaitSuccess()

    assert(clonedGraph isIsomorphicWith graph)

  }

  "Alexandre Bertails must appear as an editor in new-tr.rdf" in {

    val query = AskQuery("""
prefix : <http://www.w3.org/2001/02pd/rec54#>
prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
prefix contact: <http://www.w3.org/2000/10/swap/pim/contact#>

ASK {
  graph <http://example.com/graph> {
    ?thing :editor ?ed .
    ?ed contact:fullName "Alexandre Bertails"
  }
}
""")

    val alexIsThere = asyncEngine.executeAsk(query).awaitSuccess()

    alexIsThere must be(true)

  }

}
