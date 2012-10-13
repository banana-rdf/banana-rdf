package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import scala.concurrent._
import scala.concurrent.util._
import scalaz._
import Scalaz._

class GraphStoreTest[Rdf <: RDF](
    store: RDFStore[Rdf, Future])(
        implicit diesel: Diesel[Rdf],
        reader: RDFReader[Rdf, RDFXML]) extends WordSpec with MustMatchers with BeforeAndAfterAll with TestHelper {

  import diesel._
  import ops._

  val graphStore = GraphStore[Rdf, Future](store)

  override def afterAll(): Unit = {
    store.shutdown()
  }

  val graph: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  ).graph

  val foo: Rdf#Graph = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  "getNamedGraph should retrieve the graph added with appendToGraph" in {
    val u1 = URI("http://example.com/graph")
    val u2 = URI("http://example.com/graph2")
    val r = for {
      _ <- graphStore.removeGraph(u1)
      _ <- graphStore.removeGraph(u2)
      _ <- graphStore.appendToGraph(u1, graph)
      _ <- graphStore.appendToGraph(u2, graph2)
      rGraph <- graphStore.getGraph(u1)
      rGraph2 <- graphStore.getGraph(u2)
    } yield {
      assert(rGraph isIsomorphicWith graph)
      assert(rGraph2 isIsomorphicWith graph2)
    }
    r.getOrFail()
  }

  "appendToGraph should be equivalent to graph union" in {
    val u = URI("http://example.com/graph")
    val r = for {
      _ <- graphStore.removeGraph(u)
      _ <- graphStore.appendToGraph(u, graph)
      _ <- graphStore.appendToGraph(u, graph2)
      rGraph <- graphStore.getGraph(u)
    } yield {
      assert(rGraph isIsomorphicWith union(List(graph, graph2)))
    }
    r.getOrFail()
  }

  "patchGraph should delete and insert triples as expected" in {
    val u = URI("http://example.com/graph")
    val r = for {
      _ <- graphStore.removeGraph(u)
      _ <- graphStore.appendToGraph(u, foo)
      _ <- graphStore.patchGraph(u,
        (URI("http://example.com/foo") -- rdf("foo") ->- "foo").graph.toIterable,
        (URI("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
      rGraph <- graphStore.getGraph(u)
    } yield {
      val expected = (
        URI("http://example.com/foo")
        -- rdf("bar") ->- "bar"
        -- rdf("baz") ->- "baz"
      ).graph
      assert(rGraph isIsomorphicWith expected)
    }
    r.getOrFail()
  }

}
