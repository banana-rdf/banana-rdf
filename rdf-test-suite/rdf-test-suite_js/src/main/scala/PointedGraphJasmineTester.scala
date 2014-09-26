package org.w3.banana.jasmine.test

import org.w3.banana.{ RDFStore => RDFStoreInterface, _ }

import scala.scalajs.test.JasmineTest

/**
 * Ported by Antonio Garrotte from rdf-test-suite in scala.tests to Jasmine Tests
 */
abstract class PointedGraphJasmineTester[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends JasmineTest {

  import ops._

  val henryURI: String = "http://bblfish.net/people/henry/card#me"
  val henry = URI(henryURI)

  describe("Two similarly constructed PointedGraphs only have plain object identity") {

    it("should work correctly with uris") {

      val u1 = URI("http://test.com/something")
      val u2 = URI("http://test.com/something")

      expect(u1.equals(u2)).toEqual(true)
      expect(u1 == u2).toEqual(true)
    }

    it("with URI pointers") {
      val pg1 = PointedGraph(henry, Graph.empty)
      val pg2 = PointedGraph(URI(henryURI))
      val pointer1: RDF#Node = pg1.pointer
      val pointer2: RDF#Node = pg2.pointer

      expect(pg1.pointer == pg2.pointer).toEqual(true)
      expect(pg1.graph.equals(pg2.graph)).toEqual(true)
      //yet
      expect(pg1.equals(pg2)).toEqual(false)
    }

    //todo: this test does appear in the rdf-test-suite, and is not required by the spec of PointedGraph
    it("with bnode pointers") {
      val bnode = BNode()
      val graph = Graph(Triple(bnode, rdf.first, Literal.tagged("Henry", Lang("en"))))
      val pg1 = PointedGraph(bnode, graph)
      val pg2 = PointedGraph(bnode, graph)

      expect(pg1.equals(pg2)).toEqual(false)
    }

  }

}
