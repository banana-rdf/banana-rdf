package org.w3.banana

import org.scalatest._
import org.w3.banana.diesel._
import org.w3.banana.syntax._
import scala.concurrent._
import scalaz.Scalaz._

class GraphStoreTest[Rdf <: RDF, A](
  store: A)(
    implicit val ops: RDFOps[Rdf],
    val reader: RDFReader[Rdf, RDFXML],
    val graphStore: GraphStore[Rdf, A],
    val lifecycle: Lifecycle[Rdf, A])
    extends WordSpec with Matchers with BeforeAndAfterAll with TestHelper {

  import ops._
  import graphStore.graphStoreSyntax._
  import lifecycle.lifecycleSyntax._

  val foaf = FOAFPrefix[Rdf]

  override def afterAll(): Unit = {
    super.afterAll()
    store.stop()
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
      _ <- store.removeGraph(u1)
      _ <- store.removeGraph(u2)
      _ <- store.appendToGraph(u1, graph)
      _ <- store.appendToGraph(u2, graph2)
      rGraph <- store.getGraph(u1)
      rGraph2 <- store.getGraph(u2)
    } yield {
      assert(rGraph isIsomorphicWith graph)
      assert(rGraph2 isIsomorphicWith graph2)
    }
    r.getOrFail()
  }

  "appendToGraph should be equivalent to graph union" in {
    val u = URI("http://example.com/graph")
    val r = for {
      _ <- store.removeGraph(u)
      _ <- store.appendToGraph(u, graph)
      _ <- store.appendToGraph(u, graph2)
      rGraph <- store.getGraph(u)
    } yield {
      assert(rGraph isIsomorphicWith union(List(graph, graph2)))
    }
    r.getOrFail()
  }

  "delete/insert triples" in {
    val u = URI("http://example.com/graph")
    val r = for {
      _ <- store.removeGraph(u)
      _ <- store.appendToGraph(u, foo)
      _ <- store.removeTriples(u, (URI("http://example.com/foo") -- rdf("foo") ->- "foo").graph.triples.to[Iterable])
      _ <- store.appendToGraph(u, (URI("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
      rGraph <- store.getGraph(u)
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
