package org.w3.banana

import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._
import akka.actor.ActorSystem
import akka.util.Timeout

abstract class AsyncGraphStoreTest[Rdf <: RDF](
  ops: RDFOperations[Rdf],
  dsl: Diesel[Rdf],
  graphUnion: GraphUnion[Rdf],
  graphStore: GraphStore[Rdf],
  reader: RDFReader[Rdf, RDFXML],
  iso: GraphIsomorphism[Rdf]
) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import iso._
  import ops._
  import dsl._
  import graphUnion._

  val system = ActorSystem("jena-asyncstore-test", AsyncRDFStore.DEFAULT_CONFIG)
  implicit val timeout = Timeout(1000)
  val store = AsyncGraphStore(graphStore, system)

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

  "getNamedGraph should retrieve the graph added with addNamedGraph" in {
    for {
      _ <- store.addNamedGraph(URI("http://example.com/graph"), graph)
      _ <- store.addNamedGraph(URI("http://example.com/graph2"), graph2)
      retrievedGraph <- store.getNamedGraph(URI("http://example.com/graph"))
      retrievedGraph2 <- store.getNamedGraph(URI("http://example.com/graph2"))
    } {
      assert(graph isIsomorphicWith retrievedGraph)
      assert(graph2 isIsomorphicWith retrievedGraph2)
    }
  }

  "appendToNamedGraph should be equivalent to graph union" in {
    for {
      _ <- store.addNamedGraph(URI("http://example.com/graph"), graph)
      _ <- store.appendToNamedGraph(URI("http://example.com/graph"), graph2)
      retrievedGraph <- store.getNamedGraph(URI("http://example.com/graph"))
    } {
      val unionGraph = union(graph, graph2)
      assert(unionGraph isIsomorphicWith retrievedGraph)
    }
  }

  override def afterAll(): Unit = {
    system.shutdown()
  }

}
