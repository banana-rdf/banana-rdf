package org.w3.banana

import org.scalatest.{Matchers, WordSpec}


class GraphTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec with Matchers {

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
      isomorphism(foo, fooReference) shouldEqual true
      isomorphism(bar, barReference) shouldEqual true
      (! isomorphism(foo, bar))  shouldEqual true
      graphSize(result) shouldEqual graphSize(foobar)
      isomorphism(foobar, result)  shouldEqual true
    }

    "union of Nil must return an empty graph" in {
      val result: Rdf#Graph = union(Nil)
      isomorphism(result, emptyGraph)  shouldEqual true
      graphSize(result) shouldEqual 0
    }

    "union of a single graph must return an isomorphic graph" in {
      val result = union(foo :: Nil)
      isomorphism(result, foo)  shouldEqual true
    }
  }

  "diff tests" should {

    "removing one triple in a 1 triple graph must return the empty graph" in {
      isomorphism(diff(foo1gr, foo1gr), emptyGraph)  shouldEqual true
    }

    "removing one triple in a 2 triple graph must leave the other triple in the graph" in {

      val d = diff(foo1gr union bar1gr, foo1gr)
      graphSize(d) shouldEqual graphSize(bar1gr)
      isomorphism(d, bar1gr)  shouldEqual true

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "Alexandre"))
      graphSize(d2) shouldEqual 1
      isomorphism(d2, bnNameGr(1, "Henry"))  shouldEqual true

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, bnNameGr(2, "Alexandre"))
      isomorphism(d3, bnNameGr(1, "Henry"))  shouldEqual true

    }

    "removing a triple that is not present in a 2 triple graph must return the same graph" in {
      val d = diff(foo1gr union bar1gr, bnNameGr(1, "George"))
      graphSize(d) shouldEqual graphSize(foo1gr union bar1gr)
      isomorphism(d, foo1gr union bar1gr)  shouldEqual true

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "{}"))
      isomorphism(d2, oneBNGraph)  shouldEqual true

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, foo1gr)
      isomorphism(d3, twoBNGraph)  shouldEqual true

      val d4 = diff(twoBNGraph, bnNameGr(1, "And now for something completely different"))
      isomorphism(d4, twoBNGraph)  shouldEqual true

    }

  }

}
