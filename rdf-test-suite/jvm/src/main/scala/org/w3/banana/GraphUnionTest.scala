package org.w3.banana

import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.scalatest._

abstract class GraphUnionTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with Matchers {

  import ops._

  val foo = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  val fooReference = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  val bar = (
    URI("http://example.com/foo")
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  val barReference = (
    URI("http://example.com/foo")
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  val foobar = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
    -- rdf("baz") ->- "baz"
  ).graph

  "union must compute the union of two graphs, and should not touch the graphs" in {
    val result = union(foo :: bar :: Nil)
    isomorphism(foo, fooReference) should be(true)
    isomorphism(bar, barReference) should be(true)
    isomorphism(foo, bar) should be(false)
    isomorphism(foobar, result) should be(true)
  }

  "union of Nil must return an empty graph" in {
    val result: Rdf#Graph = union(Nil)
    isomorphism(result, emptyGraph) should be(true)
  }

  "union of a single graph must return an isomorphic graph" in {
    val result = union(foo :: Nil)
    isomorphism(result, foo) should be(true)
  }

}
