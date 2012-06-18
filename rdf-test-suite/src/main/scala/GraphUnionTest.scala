package org.w3.banana

import org.scalatest._
import org.scalatest.matchers._

abstract class GraphUnionTest[Rdf <: RDF]()(implicit diesel: Diesel[Rdf], iso: GraphIsomorphism[Rdf])
extends WordSpec with MustMatchers {

  import diesel._
  import ops._
  import graphUnion._

  val foo = (
    uri("http://example.com/foo")
      -- rdf("foo") ->- "foo"
      -- rdf("bar") ->- "bar"
  ).graph

  val fooReference = (
    uri("http://example.com/foo")
      -- rdf("foo") ->- "foo"
      -- rdf("bar") ->- "bar"
  ).graph

  val bar = (
    uri("http://example.com/foo")
      -- rdf("bar") ->- "bar"
      -- rdf("baz") ->- "baz"
  ).graph

  val barReference = (
    uri("http://example.com/foo")
      -- rdf("bar") ->- "bar"
      -- rdf("baz") ->- "baz"
  ).graph

  val foobar = (
    uri("http://example.com/foo")
      -- rdf("foo") ->- "foo"
      -- rdf("bar") ->- "bar"
      -- rdf("baz") ->- "baz"
  ).graph

  "union must compute the union of two graphs, and should not touch the graphs" in {
    val result = union(foo, bar)
    iso.isomorphism(foo, fooReference) must be (true)
    iso.isomorphism(bar, barReference) must be (true)
    iso.isomorphism(foobar, result) must be (true)
  }

}
