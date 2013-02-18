package org.w3.banana

import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.scalatest._
import org.scalatest.matchers._

abstract class GraphUnionTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with MustMatchers {

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
    isomorphism(foo, fooReference) must be(true)
    isomorphism(bar, barReference) must be(true)
    isomorphism(foo, bar) must be(false)
    isomorphism(foobar, result) must be(true)
  }

  "union of Nil must return an empty graph" in {
    val result: Rdf#Graph = union(Nil)
    isomorphism(result, emptyGraph) must be(true)
  }

  "union of a single graph must return an isomorphic graph" in {
    val result = union(foo :: Nil)
    isomorphism(result, foo) must be(true)
  }

}
