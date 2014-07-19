package org.w3.banana.rdfstorew

import org.w3.banana._


import scala.scalajs.test.JasmineTest

class PointedGraphJasmineTester[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
  extends JasmineTest {

  import ops._

  val henryURI: String = "http://bblfish.net/people/henry/card#me"
  val henry = URI(henryURI)

  describe("Two similarly constructed PointedGraphs only have plain object identity") {

    it("with URI pointers") {
      val pg1 = PointedGraph(henry, Graph.empty)
      val pg2 = PointedGraph(URI(henryURI))
      expect(pg1.pointer == pg2.pointer).toEqual(true)
      expect(pg1.graph.equals(pg2.graph)).toEqual(true)
      //yet
      expect(pg1.equals(pg2)).toEqual(false)
    }

    it("with bnode pointers") {
      val bnode = BNode()
      val graph = Graph(Triple(bnode, rdf.first, Literal.tagged("Henry", Lang("en"))))
      val pg1 = PointedGraph(bnode, graph)
      val pg2 = PointedGraph(bnode, graph)

      expect(pg1.equals(pg2)).toEqual(false)
    }

  }

}

object PointedGraphJasmineTesterRDFStore extends PointedGraphJasmineTester[RDFStore]

//class RDFStoreWTurtleTest extends TurtleTestSuite[RDFStore]

//class RDFStoreWGraphUnionTest extends GraphUnionTest[RDFStore]


class RDFStoreWPointedGraphTest extends PointedGraphTester[RDFStore]
/*
import org.w3.banana.diesel._

class RDFStoreWDieselGraphConstructTest extends DieselGraphConstructTest[RDFStore]

class RDFStoreWDieselGraphExplorationTest extends DieselGraphExplorationTest[RDFStore]

import org.w3.banana.binder._

class RDFStoreWCommonBindersTest extends CommonBindersTest[RDFStore]

class RDFStoreWRecordBinderTest extends RecordBinderTest[RDFStore]

import org.w3.banana.syntax._

class RDFStoreWUriSyntaxTest extends UriSyntaxTest[RDFStore]
*/