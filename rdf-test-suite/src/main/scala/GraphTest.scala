package org.w3.banana

import org.scalatest._
import org.w3.banana.diesel._

abstract class GraphTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with Matchers {

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

  "union tests" when {

    "union must compute the union of two graphs, and should not touch the graphs" in {
      val result = union(foo :: bar :: Nil)
      isomorphism(foo, fooReference) should be(true)
      isomorphism(bar, barReference) should be(true)
      isomorphism(foo, bar) should be(false)
      graphSize(result) should be(graphSize(foobar))
      isomorphism(foobar, result) should be(true)
    }

    "union of Nil must return an empty graph" in {
      val result: Rdf#Graph = union(Nil)
      isomorphism(result, emptyGraph) should be(true)
      graphSize(result) should be(0)
    }

    "union of a single graph must return an isomorphic graph" in {
      val result = union(foo :: Nil)
      isomorphism(result, foo) should be(true)
    }
  }

  "diff tests" when {
    "removing one triple in a 1 triple graph must return the empty graph" in {
      isomorphism(diff(foo1gr, foo1gr), emptyGraph) should be(true)
    }

    "removing one triple in a 2 triple graph must leave the other triple in the graph" in {

      val d = diff(foo1gr union bar1gr, foo1gr)
      graphSize(d) should be(graphSize(bar1gr))
      isomorphism(d, bar1gr) should be(true)

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "Alexandre"))
      graphSize(d2) should be(1)
      isomorphism(d2, bnNameGr(1, "Henry")) should be(true)

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, bnNameGr(2, "Alexandre"))
      isomorphism(d3, bnNameGr(1, "Henry")) should be(true)

    }

    "removing a triple that is not present in a 2 triple graph must return the same graph" in {
      val d = diff(foo1gr union bar1gr, bnNameGr(1, "George"))
      graphSize(d) should be(graphSize(foo1gr union bar1gr))
      isomorphism(d, foo1gr union bar1gr) should be(true)

      val oneBNGraph = bnNameGr(1, "Henry") union bnNameGr(1, "Alexandre")
      val d2 = diff(oneBNGraph, bnNameGr(1, "{}"))
      isomorphism(d2, oneBNGraph) should be(true)

      val twoBNGraph = bnNameGr(1, "Henry") union bnNameGr(2, "Alexandre")
      val d3 = diff(twoBNGraph, foo1gr)
      isomorphism(d3, twoBNGraph) should be(true)

      val d4 = diff(twoBNGraph, bnNameGr(1, "And now for something completely different"))
      isomorphism(d4, twoBNGraph) should be(true)

    }

  }

}
