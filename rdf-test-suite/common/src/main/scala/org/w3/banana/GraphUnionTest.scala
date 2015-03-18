package org.w3.banana

import com.inthenow.zcheck.SpecLite
import org.w3.banana.diesel._

abstract class GraphUnionTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends SpecLite {

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
    check(isomorphism(foo, fooReference))
    check(isomorphism(bar, barReference))
    check(! isomorphism(foo, bar))
    check(isomorphism(foobar, result))
  }

  "union of Nil must return an empty graph" in {
    val result: Rdf#Graph = union(Nil)
    check(isomorphism(result, emptyGraph))
  }

  "union of a single graph must return an isomorphic graph" in {
    val result = union(foo :: Nil)
    check(isomorphism(result, foo))
  }

}
