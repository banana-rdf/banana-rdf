package org.w3.banana

import org.w3.banana.diesel._
import zcheck.SpecLite

class GraphTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

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
      check(isomorphism(foo, fooReference))
      check(isomorphism(bar, barReference))
      check(! isomorphism(foo, bar))
      graphSize(result) must_==(graphSize(foobar))
      check(isomorphism(foobar, result))
    }

    "union of Nil must return an empty graph" in {
      val result: Rdf#Graph = union(Nil)
      check(isomorphism(result, emptyGraph))
      graphSize(result) must_==(0)
    }

    "union of a single graph must return an isomorphic graph" in {
      val result = union(foo :: Nil)
      check(isomorphism(result, foo))
    }
  }

  "diff tests" should {

    "removing one triple in a 1 triple graph must return the empty graph" in {
      check(isomorphism(diff(foo1gr, foo1gr), emptyGraph))
    }

    "removing one triple in a 2 triple graph must leave the other triple in the graph" in {

      val d = diff(foo1gr union bar1gr, foo1gr)
      graphSize(d) must_==(graphSize(bar1gr))
      check(isomorphism(d, bar1gr))

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "Alexandre"))
      graphSize(d2) must_==(1)
      check(isomorphism(d2, bnNameGr(1, "Henry")))

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, bnNameGr(2, "Alexandre"))
      check(isomorphism(d3, bnNameGr(1, "Henry")))

    }

    "removing a triple that is not present in a 2 triple graph must return the same graph" in {
      val d = diff(foo1gr union bar1gr, bnNameGr(1, "George"))
      graphSize(d) must_==(graphSize(foo1gr union bar1gr))
      check(isomorphism(d, foo1gr union bar1gr))

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "{}"))
      check(isomorphism(d2, oneBNGraph))

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, foo1gr)
      check(isomorphism(d3, twoBNGraph))

      val d4 = diff(twoBNGraph, bnNameGr(1, "And now for something completely different"))
      check(isomorphism(d4, twoBNGraph))

    }

  }

}
