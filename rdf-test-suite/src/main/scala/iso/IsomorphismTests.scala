package org.w3.banana.iso

import org.scalatest.{ Matchers, Suite, WordSpec }
import org.w3.banana.{ RDF, RDFOps }

/**
 * Generic Tests for  isomorphism functions that use whatever the Ops implementation chooses
 * Created by Henry Story on 13/07/2014.
 */
abstract class IsomorphismTests[Rdf <: RDF](
  implicit val ops: RDFOps[Rdf])
    extends WordSpec with IsomorphismBNodeTrait[Rdf] with Matchers { self: Suite =>

  import ops._
  import org.w3.banana.diesel._

  "simple isomorphism tests" when {

    "a 1 triple ground graph" in {
      val g1 = (hjs -- foaf.name ->- "Henry Story").graph
      val expected = Graph(Triple(hjs, foaf.name, Literal("Henry Story")))
      isomorphism(g1, expected) should be(true)

      val nonExpected = Graph(Triple(hjs, foaf.name, Literal("Henri Story")))
      isomorphism(g1, nonExpected) should be(false)
    }

    "two grounded graphs with 2 relations" in {
      val g1 = groundedGraph
      val expected = groundedGraph
      isomorphism(g1, expected) should be(true)
    }

    "list of size 1" in {
      val g = list(1, "h")
      val expected = list(1, "g")
      isomorphism(g, expected) should be(true)
    }

    "list of size 2" in {
      val g = list(2, "h")
      val expected = list(2, "g")
      isomorphism(g, expected) should be(true)
    }

    "list of size 5" in {
      val g = list(5, "h")
      val expected = list(5, "g")
      isomorphism(g, expected) should be(true)
    }

  }

  "file isomorphism tests" in {
    //run on longer examples
  }
}
