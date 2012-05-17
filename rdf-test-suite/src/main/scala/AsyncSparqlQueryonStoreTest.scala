package org.w3.banana

import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._
import akka.actor.ActorSystem
import akka.util.Timeout

abstract class AsyncSparqlQueryOnStoreTest[Store, Rdf <: RDF, Sparql <: SPARQL](
  ops: RDFOperations[Rdf],
  dsl: Diesel[Rdf],
  iso: GraphIsomorphism[Rdf],
  sparqlOps: SPARQLOperations[Rdf, Sparql],
  underlyingStore: Store,
  storeFunc: Store => RDFStore[Rdf],
  engineFunc: Store => SPARQLEngine[Rdf, Sparql]
) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import iso._
  import ops._
  import dsl._
  import sparqlOps._

  val system = ActorSystem("jena-asynsparqlquery-test", AsyncRDFStore.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)
  val store = storeFunc(underlyingStore)

  val asyncEngine = AsyncSPARQLEngine(engineFunc(underlyingStore), system)

  val foaf = FOAFPrefix(ops)

  val graph: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
      -- foaf.name ->- "Alexandre".lang("fr")
      -- foaf.knows ->- (
        uri("http://bblfish.net/#hjs")
          -- foaf.name ->- "Henry Story"
          -- foaf.currentProject ->- uri("http://webid.info/")
      )
  ).graph

  override def beforeAll(): Unit = {
    store.addNamedGraph(IRI("http://example.com/graph"), graph)
    store.addNamedGraph(IRI("http://example.com/graph2"), graph2)
  }

  override def afterAll(): Unit = {
    system.shutdown()
  }

  "betehess must know henry" in {

    val query = AskQuery("""
prefix foaf: <http://xmlns.com/foaf/0.1/>

ASK {
  GRAPH <http://example.com/graph2> {
    [] foaf:knows <http://bblfish.net/#hjs>
  }
}
""")

    for {
      alexKnowsHenry <- asyncEngine.executeAsk(query)
    } {
      alexKnowsHenry must be (true)
    }

  }

}
