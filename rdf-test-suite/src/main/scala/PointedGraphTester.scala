package org.w3.banana

import org.scalatest.{ Matchers, WordSpec }

/**
 *  see org.w3.banana.binder.CommonBindersTest for more tests
 */
class PointedGraphTester[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
    extends WordSpec with Matchers {

  import ops._

  val henryURI: String = "http://bblfish.net/people/henry/card#me"
  val henry = URI(henryURI)

  "Two similarly constructed PointedGraphs only have plain object identity" when {

    "with URI pointers" in {
      val pg1 = PointedGraph(henry, Graph.empty)
      val pg2 = PointedGraph(URI(henryURI))
      pg1.pointer should be(pg2.pointer)
      pg1.graph should be(pg2.graph)
      //yet
      pg1 should not be pg2
    }

    "with bnode pointers" in {
      val bnode = BNode()
      val graph = Graph(Triple(bnode, rdf.first, Literal.tagged("Henry", Lang("en"))))
      val pg1 = PointedGraph(bnode, graph)
      val pg2 = PointedGraph(bnode, graph)

      pg1 should not be pg2
    }

  }

}
