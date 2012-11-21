package org.w3.banana.plantain

import org.w3.banana._
import org.scalatest._
import org.scalatest.matchers._
import Plantain._
import LDPCommand._
import scala.concurrent.ExecutionContext.Implicits.global

class PlantainLDPSTest extends LDPSTest[Plantain]({
  PlantainLDPS(null, null)
})

class LDPSTest[Rdf <: RDF](
  ldps: LDPS[Rdf])(
  implicit diesel: Diesel[Rdf],
  reader: RDFReader[Rdf, RDFXML]) extends WordSpec with MustMatchers with BeforeAndAfterAll {

  import diesel._
  import ops._

  val foaf = FOAFPrefix[Rdf]

  override def afterAll(): Unit = {
    ldps.shutdown()
  }

  val graph: Rdf#Graph = (
    URI("#me")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    URI("#me")
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

  "CreateLDPR should create an LDPR with the given graph -- with given uri" in {
    val ldpcUri = URI("http://example.com/foo")
    val ldprUri = URI("http://example.com/foo/betehess")
    val script = for {
      ldpc <- ldps.createLDPC(ldpcUri)
      _ <- ldpc.execute(createLDPR(Some(ldprUri), graph))
      rGraph <- ldpc.execute(getLDPR(ldprUri))
      _ <- ldps.deleteLDPC(ldpcUri)
    } yield {
      assert(rGraph isIsomorphicWith graph)
    }
    script.getOrFail(scala.concurrent.duration.Duration.create("10s"))
  }

//  "getNamedGraph should retrieve the graph added with appendToGraph" in {
//    val u1 = URI("http://example.com/foo/betehess")
//    val u2 = URI("http://example.com/foo/alexandre")
//    val r = for {
//      ldpc <- ldps.createLDPC(URI("http://example.com/foo"))
//      _ <- ldpc.execute()
//      _ <- graphStore.appendToGraph(u1, graph)
//      _ <- graphStore.appendToGraph(u2, graph2)
//      rGraph <- graphStore.getGraph(u1)
//      rGraph2 <- graphStore.getGraph(u2)
//    } yield {
//      assert(rGraph isIsomorphicWith graph)
//      assert(rGraph2 isIsomorphicWith graph2)
//    }
//    r.getOrFail()
//  }

//  "appendToGraph should be equivalent to graph union" in {
//    val u = URI("http://example.com/graph")
//    val r = for {
//      _ <- graphStore.removeGraph(u)
//      _ <- graphStore.appendToGraph(u, graph)
//      _ <- graphStore.appendToGraph(u, graph2)
//      rGraph <- graphStore.getGraph(u)
//    } yield {
//      assert(rGraph isIsomorphicWith union(List(graph, graph2)))
//    }
//    r.getOrFail()
//  }
//
//  "patchGraph should delete and insert triples as expected" in {
//    val u = URI("http://example.com/graph")
//    val r = for {
//      _ <- graphStore.removeGraph(u)
//      _ <- graphStore.appendToGraph(u, foo)
//      _ <- graphStore.patchGraph(u,
//        (URI("http://example.com/foo") -- rdf("foo") ->- "foo").graph.toIterable,
//        (URI("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
//      rGraph <- graphStore.getGraph(u)
//    } yield {
//      val expected = (
//        URI("http://example.com/foo")
//        -- rdf("bar") ->- "bar"
//        -- rdf("baz") ->- "baz"
//      ).graph
//      assert(rGraph isIsomorphicWith expected)
//    }
//    r.getOrFail()
//  }

}
