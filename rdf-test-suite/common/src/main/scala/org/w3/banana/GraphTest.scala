package org.w3.banana

import org.w3.banana.diesel._
import org.scalatest.WordSpec

class GraphTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._

  def exuri(foo: String = "foo"): Rdf#URI = URI("http://example.com/" + foo)
  val foo1gr: Rdf#Graph = Graph(Triple(exuri(), rdf("foo"), Literal("foo")))
  val bar1gr: Rdf#Graph = Graph(Triple(exuri(), rdf("bar"), Literal("bar")))

  def exbnode(n: Int = 1): Rdf#Node = BNode("ex" + n)

  def bnNameGr(n: Int = 1, name: String) = Graph(Triple(exbnode(n), rdf("knows"), Literal(name)))

  val foo = (
    exuri()
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  val fooReference = (
    exuri()
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  val bar = (
    exuri()
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  val barReference = (
    exuri()
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  val foobar = (
    exuri()
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  "union test" should {

    "union must compute the union of two graphs, and should not touch the graphs" in {
      val result = union(foo :: bar :: Nil)
      assert(isomorphism(foo, fooReference))
      assert(isomorphism(bar, barReference))
      assert(! isomorphism(foo, bar))
      assert(graphSize(result) === (graphSize(foobar)))
      assert(isomorphism(foobar, result))
    }

    "union of Nil must return an empty graph" in {
      val result: Rdf#Graph = union(Nil)
      assert(isomorphism(result, emptyGraph))
      assert(graphSize(result) === (0))
    }

    "union of a single graph must return an isomorphic graph" in {
      val result = union(foo :: Nil)
      assert(isomorphism(result, foo))
    }
  }

  "diff tests" should {

    "removing one triple in a 1 triple graph must return the empty graph" in {
      assert(isomorphism(diff(foo1gr, foo1gr), emptyGraph))
    }

    "removing one triple in a 2 triple graph must leave the other triple in the graph" in {

      val d = diff(foo1gr union bar1gr, foo1gr)
      assert(graphSize(d) === (graphSize(bar1gr)))
      assert(isomorphism(d, bar1gr))

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "Alexandre"))
      assert(graphSize(d2) === (1))
      assert(isomorphism(d2, bnNameGr(1, "Henry")))

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, bnNameGr(2, "Alexandre"))
      assert(isomorphism(d3, bnNameGr(1, "Henry")))

    }

    "removing a triple that is not present in a 2 triple graph must return the same graph" in {
      val d = diff(foo1gr union bar1gr, bnNameGr(1, "George"))
      assert(graphSize(d) === (graphSize(foo1gr union bar1gr)))
      assert(isomorphism(d, foo1gr union bar1gr))

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "{}"))
      assert(isomorphism(d2, oneBNGraph))

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, foo1gr)
      assert(isomorphism(d3, twoBNGraph))

      val d4 = diff(twoBNGraph, bnNameGr(1, "And now for something completely different"))
      assert(isomorphism(d4, twoBNGraph))

    }

  }

}
