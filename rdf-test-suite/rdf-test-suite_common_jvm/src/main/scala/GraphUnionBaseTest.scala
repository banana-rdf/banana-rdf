package org.w3.banana

import com.github.inthenow.jasmine.JSpec



import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.w3.banana.binder._
import scala.concurrent.ExecutionContext
//import scala.scalajs.concurrent.JSExecutionContext
import scalaz.Scalaz._
import scala.util._
import scala.collection.immutable.ListMap
import java.io._
import scala.concurrent.Future
import org.w3.banana.{ RDFStore => RDFStoreInterface }

//import scala.scalajs.js
//import scala.scalajs.test.JasmineTest


/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */


abstract trait GraphUnionBaseTest[Rdf <: RDF] extends JSpec {

  implicit val ops: RDFOps[Rdf]


  import ops._
  import syntax._



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

  describe("Graph union ops") {
    it("union must compute the union of two graphs, and should not touch the graphs") {
      val result = union(foo :: bar :: Nil)
      expect(isomorphism(foo, fooReference)).toEqual(true)
      expect(isomorphism(bar, barReference)).toEqual(true)
      expect(isomorphism(foo, bar)).toEqual(false)
      expect(isomorphism(foobar, result)).toEqual(true)
    }

    it("union of Nil must return an empty graph") {
      val result: Rdf#Graph = union(Nil)
      expect(isomorphism(result, emptyGraph)).toEqual(true)
    }

    it("union of a single graph must return an isomorphic graph") {
      val result = union(foo :: Nil)
      expect(isomorphism(result, foo)).toEqual(true)
    }
  }
}
