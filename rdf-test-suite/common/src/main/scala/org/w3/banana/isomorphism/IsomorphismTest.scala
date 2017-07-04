package org.w3.banana.isomorphism

import org.scalatest.WordSpec
import org.w3.banana._

/**
 * Generic Tests for  isomorphism functions that use whatever the Ops implementation chooses
 */
class IsomorphismTest[Rdf <: RDF](implicit val ops: RDFOps[Rdf])
extends WordSpec with IsomorphismBNodeTrait[Rdf] {

  import ops._
  import org.w3.banana.diesel._

  "simple isomorphism tests" should {

    "a 1 triple ground graph" in {
      val g1 = (hjs -- foaf.name ->- "Henry Story").graph
      val expected = Graph(Triple(hjs, foaf.name, Literal("Henry Story")))
      assert(isomorphism(g1, expected))

      val nonExpected = Graph(Triple(hjs, foaf.name, Literal("Henri Story")))
      assert(! isomorphism(g1, nonExpected))
    }

    "two grounded graphs with 2 relations" in {
      val g1 = groundedGraph
      val expected = groundedGraph
      assert(isomorphism(g1, expected))
    }

    "list of size 1" in {
      val g = list(1, "h")
      val expected = list(1, "g")
      assert(isomorphism(g, expected))
    }

    "list of size 2" in {
      val g = list(2, "h")
      val expected = list(2, "g")
      assert(isomorphism(g, expected))
    }

    "list of size 5" in {
      val g = list(5, "h")
      val expected = list(5, "g")
      assert(isomorphism(g, expected))
    }

  }

}
