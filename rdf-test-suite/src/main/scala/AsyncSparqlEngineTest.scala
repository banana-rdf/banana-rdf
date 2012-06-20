package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import akka.actor.ActorSystem
import akka.util.Timeout
import akka.dispatch._
import akka.util.duration._

abstract class AsyncSparqlEngineTest[Rdf <: RDF, Sparql <: SPARQL]()(
  implicit reader: RDFReader[Rdf, RDFXML],
  diesel: Diesel[Rdf],
  iso: GraphIsomorphism[Rdf],
  sparqlOps: SPARQLOperations[Rdf, Sparql],
  store: RDFStore[Rdf, Sparql]
) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import diesel._
  import iso._
  import ops._
  import sparqlOps._

  val system = ActorSystem("jena-asynsparqlquery-test", AsyncRDFStore.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)

  val asyncEngine = AsyncRDFStore(store, system)

  val file = new java.io.File("rdf-test-suite/src/main/resources/new-tr.rdf")

  val graph = reader.read(file, "http://example.com") getOrElse sys.error("ouch")

  override def beforeAll(): Unit = {
    store.addNamedGraph(URI("http://example.com/graph"), graph)
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

    val names: Iterable[String] = solutionIterator(Await.result(asyncEngine.executeSelect(query), 1.second)) map { row => row("name").flatMap(_.as[String]) getOrElse sys.error("") }

    names must contain ("Alexandre Bertails")

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

    val clonedGraph = Await.result(asyncEngine.executeConstruct(query), 1.second)

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

    val alexIsThere = Await.result(asyncEngine.executeAsk(query), 1.second)

    alexIsThere must be (true)

  }



}
