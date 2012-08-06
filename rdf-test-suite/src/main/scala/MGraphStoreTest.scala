package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._
import scalaz._
import Scalaz._
import util._
import BananaException.bananaCatch

abstract class MGraphStoreTest[Rdf <: RDF, M[_]](implicit diesel: Diesel[Rdf],
    reader: BlockingReader[Rdf#Graph, RDFXML],
    bind: Bind[M],
    extractor: UnsafeExtractor[M]) extends WordSpec with MustMatchers {

  def store: MGraphStore[Rdf, M]

  import diesel._
  import ops._
  import extractor._

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

  val foo: Rdf#Graph = (
    uri("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  "getNamedGraph should retrieve the graph added with appendToGraph" in {
    unsafeExtract {
      val u1 = uri("http://example.com/graph")
      val u2 = uri("http://example.com/graph2")
      for {
        _ <- store.removeGraph(u1)
        _ <- store.removeGraph(u2)
        _ <- store.appendToGraph(u1, graph)
        _ <- store.appendToGraph(u2, graph2)
        rGraph <- store.getGraph(u1)
        rGraph2 <- store.getGraph(u2)
      } yield {
        assert(rGraph isIsomorphicWith graph)
        assert(rGraph2 isIsomorphicWith graph2)
        true
      }
    } must be(Success(true))
  }

  "appendToGraph should be equivalent to graph union" in {
    unsafeExtract {
      val u = uri("http://example.com/graph")
      for {
        _ <- store.removeGraph(u)
        _ <- store.appendToGraph(u, graph)
        _ <- store.appendToGraph(u, graph2)
        rGraph <- store.getGraph(u)
      } yield {
        assert(rGraph isIsomorphicWith union(List(graph, graph2)))
        true
      }
    } must be(Success(true))
  }

  "patchGraph should delete and insert triples as expected" in {
    unsafeExtract {
      val u = uri("http://example.com/graph")
      for {
        _ <- store.removeGraph(u)
        _ <- store.appendToGraph(u, foo)
        _ <- store.patchGraph(u,
                              (uri("http://example.com/foo") -- rdf("foo") ->- "foo").graph,
                              (uri("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
        rGraph <- store.getGraph(u)
      } yield {
        val expected = (
          uri("http://example.com/foo")
          -- rdf("bar") ->- "bar"
          -- rdf("baz") ->- "baz"
        ).graph
        assert(rGraph isIsomorphicWith expected)
        true
      }
    } must be(Success(true))
  }

}
