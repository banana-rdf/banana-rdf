package org.w3.banana.jasmine.test

import org.w3.banana._
import org.w3.banana.syntax._
import org.w3.banana.diesel._
import org.w3.banana.binder._
import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext
import scalaz.Scalaz._
import scala.util._
import scala.collection.immutable.ListMap
import java.io._
import org.scalatest.EitherValues._
import scala.concurrent.Future
import org.w3.banana.{ RDFStore => RDFStoreInterface }

import scala.scalajs.js
import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class GraphStoreJasmineTest[Rdf <: RDF](store: RDFStoreInterface[Rdf])(
  implicit ops: RDFOps[Rdf])
    extends JasmineTest {

  import ops._
  import syntax._
  import JSExecutionContext.Implicits.queue

  val foaf = FOAFPrefix[Rdf]

  val graphStore = GraphStore[Rdf](store)

  val graph: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
  ).graph

  val graph2: Rdf#Graph = (
    bnode("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/")
    )
  ).graph

  val foo: Rdf#Graph = (
    URI("http://example.com/foo")
    -- rdf("foo") ->- "foo"
    -- rdf("bar") ->- "bar"
  ).graph

  describe("RDFStore Banana Interface") {

    it("getNamedGraph should retrieve the graph added with appendToGraph") {
      jasmine.Clock.useMock()

      val u1 = URI("http://example.com/graph")
      val u2 = URI("http://example.com/graph2")

      graphStore.removeGraph(u1)
      jasmine.Clock.tick(10)
      graphStore.removeGraph(u2)
      jasmine.Clock.tick(10)
      graphStore.appendToGraph(u1, graph)
      jasmine.Clock.tick(10)
      val rGraph = graphStore.getGraph(u1)
      jasmine.Clock.tick(10)
      graphStore.appendToGraph(u2, graph2)
      jasmine.Clock.tick(10)
      val rGraph2 = graphStore.getGraph(u2)
      jasmine.Clock.tick(10)

      rGraph.onSuccess {
        case rg =>
          expect(rg isIsomorphicWith graph).toEqual(true)
          rGraph2.onSuccess {
            case rg2 =>
              expect(rg2 isIsomorphicWith graph2).toEqual(true)
          }
      }

      val r = for {
        _ <- graphStore.removeGraph(u1)
        _ <- graphStore.removeGraph(u2)
        _ <- graphStore.appendToGraph(u1, graph)
        _ <- graphStore.appendToGraph(u2, graph2)
        rGraph <- graphStore.getGraph(u1)
        rGraph2 <- graphStore.getGraph(u2)
      } yield {
        expect(rGraph isIsomorphicWith graph).toEqual(false)
        expect(rGraph2 isIsomorphicWith graph2).toEqual(true)
      }

      jasmine.Clock.tick(10)
      r.onSuccess {
        case res =>
          println("RESULT!!!")
      }

    }

    it("patchGraph should delete and insert triples as expected") {
      var graphStore = GraphStore[Rdf](store)
      jasmine.Clock.useMock()
      val u = URI("http://example.com/graph")

      graphStore.removeGraph(u)
      jasmine.Clock.tick(10)
      graphStore.appendToGraph(u, foo)
      jasmine.Clock.tick(10)
      graphStore.patchGraph(u,
        (URI("http://example.com/foo") -- rdf("foo") ->- "foo").graph.toIterable,
        (URI("http://example.com/foo") -- rdf("baz") ->- "baz").graph)
      jasmine.Clock.tick(10)
      val rGraph = graphStore.getGraph(u)
      jasmine.Clock.tick(10)
      val expected = (
        URI("http://example.com/foo")
        -- rdf("bar") ->- "bar"
        -- rdf("baz") ->- "baz"
      ).graph

      rGraph.onSuccess {
        case g =>
          expect(g isIsomorphicWith expected).toEqual(true)
      }

    }

  }

}
