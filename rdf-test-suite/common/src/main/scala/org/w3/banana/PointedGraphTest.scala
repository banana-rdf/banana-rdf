package org.w3.banana

import org.scalatest.WordSpec


/**
 *  see org.w3.banana.binder.CommonBindersTest for more tests
 */
class PointedGraphTest[Rdf <: RDF](implicit ops: RDFOps[Rdf]) extends WordSpec {

  import ops._

  val henryURI: String = "http://bblfish.net/people/henry/card#me"
  val henry = URI(henryURI)

  "Two similarly constructed PointedGraphs only have plain object identity" should {

    "with URI pointers" in {
      val pg1 = PointedGraph(henry, Graph.empty)
      val pg2 = PointedGraph(URI(henryURI))
      assert(pg1.pointer === (pg2.pointer))
      assert(pg1.graph === (pg2.graph))
      //yet
      assert(pg1 !==(pg2) )
    }

    "with bnode pointers" in {
      val bnode = BNode()
      val graph = Graph(Triple(bnode, rdf.first, Literal.tagged("Henry", Lang("en"))))
      val pg1 = PointedGraph(bnode, graph)
      val pg2 = PointedGraph(bnode, graph)

      assert(pg1 !== (pg2) )
    }

  }

}
