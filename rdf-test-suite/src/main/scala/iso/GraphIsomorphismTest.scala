package org.w3.banana.plantain.iso

import org.scalatest.{ Suite, Matchers, WordSpec }
import org.w3.banana.{ RDF, RDFOps }
import org.w3.banana.iso.{ IsomorphismBNodeTrait, GraphIsomorphism }

import scala.util.Success

/**
 * Created by hjs on 05/09/2014.
 */
class GraphIsomorphismTest[Rdf <: RDF](graphIsomorphism: GraphIsomorphism[Rdf])(
  implicit val ops: RDFOps[Rdf])
  extends WordSpec with IsomorphismBNodeTrait[Rdf] with Matchers { self: Suite =>

  import ops._
  import graphIsomorphism._
  import org.w3.banana.diesel._

  "test groundTripleFilter(graph)" when {

    "a completely grounded graph ( no blank nodes ) " in {
      val (grounded, nongrounded) = groundTripleFilter(groundedGraph)
      grounded.triples foreach { triple =>
        assert(groundedGraph.contains(triple))
      }
      nongrounded should equal(emptyGraph)
    }

    "an ungrounded graph ( all statements contain a bnode )" in {
      val rel2Graph = bnAlexRel2Graph()
      val (grounded, nongrounded) = groundTripleFilter(rel2Graph)
      grounded.size should equal(0)
      nongrounded.size should equal(rel2Graph.size)
      nongrounded.triples foreach { triple =>
        assert(rel2Graph.contains(triple))
      }
    }

    "a graph with grounded and ungrounded statements " in {
      val rel2Graph = bnAlexRel2Graph()
      val (grounded, nongrounded) = groundTripleFilter(groundedGraph.graph union rel2Graph)
      grounded.size should be(groundedGraph.size)
      grounded.triples foreach { triple =>
        assert(groundedGraph.contains(triple))
      }
      nongrounded.size should be(rel2Graph.size)
      nongrounded.triples foreach { triple =>
        assert(rel2Graph.contains(triple))
      }
    }

  }

  "test bnode mapping solutions " when {
    "two grounded graphs with one relation" in {
      val g1 = (hjs -- foaf.name ->- "Henry Story").graph
      val expected = Graph(Triple(hjs, foaf("name"), Literal("Henry Story")))
      val answer = findAnswer(g1, expected)
      answer should equal(Success(List()))
    }

    "two grounded graphs with 2 relations" in {
      val g1 = groundedGraph
      val expected = groundedGraph
      val answer = findAnswer(g1, expected)
      answer should equal(Success(List()))
    }

    "two graphs with 2 relations and 2 bnodes each" in {
      for (
        l <- findPossibleMappings(
          bnAlexRel1Graph(1) union bnAntonioRel1Graph(1),
          bnAlexRel1Graph(2) union bnAntonioRel1Graph(2))
      ) {
        //with this system of categorisation the categories are very light
        // and they don't distinguish the literals
        //also the returned set covers symmetric results - this can also be optimised!
        l.size should be(4)
      }

      val answer = findAnswer(
        bnAlexRel1Graph(1) union bnAntonioRel1Graph(1),
        bnAlexRel1Graph(2) union bnAntonioRel1Graph(2))
      answer.isSuccess should be(true)
      answer.get.size should be(2)
      answer.get should contain(alex(1) -> alex(2))
      answer.get should contain(antonio(1) -> antonio(2))
    }

    """two graphs with 3 relations each.
      | But one category has 1 solution the other that has two.
      | The category with 1 solutions must be shown first""".stripMargin in {
      val g1 = bnAlexRel1Graph(1) union bnAntonioRel1Graph(1) union bnAlexRel2Graph(2) union bnAlexRel1Graph(0) union bnAlexRel2Graph(0)
      val g2 = bnAlexRel1Graph(3) union bnAntonioRel1Graph(3) union bnAntonioRel2Graph(4) union bnAlexRel1Graph(5) union bnAlexRel2Graph(5)
      val answers = findPossibleMappings(g1, g2)
      val answer = findAnswer(g1, g2)
      answer.isFailure should be(true)

    }
  }

  "test bnode mapping" when {

    "graphs mapped to themselves" in {
      val a1g = bnAlexRel1Graph(0)
      mapVerify(a1g, a1g, Map(alex(0) -> alex(0))) should be(Nil)

      mapVerify(bnAntonioRel1Graph(0), bnAntonioRel1Graph(0), Map((antonio(0), antonio(0)))) should be(Nil)

    }

    "1 bnode mapped" in {
      mapVerify(bnAlexRel1Graph(0), bnAlexRel1Graph(1), Map((alex(0), alex(1)))) should be(Nil)

      mapVerify(bnAntonioRel2Graph(0), bnAntonioRel1Graph(1), Map((antonio(0), antonio(1)))) should not be empty
    }

    "2 bnodes mapped" in {
      val r2g1 = bnAlexRel1Graph(0) union bnAntonioRel1Graph(0)
      val r2g2 = bnAlexRel1Graph(1) union bnAntonioRel1Graph(1)

      mapVerify(r2g1, r2g1, Map(alex(0) -> alex(0), antonio(0) -> antonio(0)))

      mapVerify(
        r2g1,
        r2g2,
        Map(alex(0) -> alex(1), antonio(0) -> antonio(1))) should be(Nil)

      //an incorrect mapping
      val v = mapVerify(
        r2g1,
        r2g2,
        Map(alex(0) -> antonio(1), antonio(0) -> alex(1)))
      v should not be empty

      //reverse test

      mapVerify(
        r2g2,
        r2g1,
        Map(alex(1) -> alex(0), antonio(1) -> antonio(0))) should be(Nil)

      //an incorrect mapping
      val v2 = mapVerify(
        r2g2,
        r2g1,
        Map(alex(1) -> antonio(0), antonio(1) -> alex(0)))
      v2 should not be empty

    }

    "list of size 1" in {
      val g1 = list(1, "h")
      val g2 = list(1, "g")

      mapVerify(g1, g2, Map(BNode("h1") -> BNode("g1"), BNode("h0") -> BNode("g0"))) should be(Nil)
    }

    "list of size 2" in {
      val g1 = list(2, "h")
      val g2 = list(2, "g")

      mapVerify(g1, g2, Map(
        BNode("h2") -> BNode("g2"),
        BNode("h1") -> BNode("g1"),
        BNode("h0") -> BNode("g0"))) should be(Nil)
    }

    "list of size 5" in {
      val g1 = list(5, "h")
      val g2 = list(5, "g")

      mapVerify(g1, g2, Map(
        BNode("h5") -> BNode("g5"),
        BNode("h4") -> BNode("g4"),
        BNode("h3") -> BNode("g3"),
        BNode("h2") -> BNode("g2"),
        BNode("h1") -> BNode("g1"),
        BNode("h0") -> BNode("g0"))) should be(Nil)
    }

    "some symmetric graphs can have more than one mapping - which are thus isomorphic" in {

      //some graphs have two mappings
      val symgrph01 = symmetricGraph(0, 1)
      val symgrph23 = symmetricGraph(2, 3)
      mapVerify(symgrph01, symgrph23, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(symgrph01, symgrph23, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)

      val symgraph01ext = symgrph01 union owlSameAs(xbn(0), xbn(0)) union owlSameAs(xbn(1), xbn(1))
      val symgraph23ext = symgrph23 union owlSameAs(xbn(2), xbn(2)) union owlSameAs(xbn(3), xbn(3))
      mapVerify(symgraph01ext, symgraph23ext, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(symgraph01ext, symgraph23ext, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)

      val oneThing01 = symgraph01ext union owlSameAs(xbn(0), xbn(1)) union owlSameAs(xbn(1), xbn(0))
      val oneThing23 = symgraph23ext union owlSameAs(xbn(2), xbn(3)) union owlSameAs(xbn(3), xbn(2))
      mapVerify(oneThing01, oneThing23, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(oneThing01, oneThing23, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)
    }

    "3 bnodes mapped" in {
      val knows3bn = bnKnowsBN(0, 1) union bnKnowsBN(1, 2) union bnKnowsBN(2, 0)

      //three different isomorphic mappings
      mapVerify(knows3bn, knows3bn, Map(xbn(0) -> xbn(0), xbn(1) -> xbn(1), xbn(2) -> xbn(2))) should be(Nil)
      mapVerify(knows3bn, knows3bn, Map(xbn(0) -> xbn(1), xbn(1) -> xbn(2), xbn(2) -> xbn(0))) should be(Nil)
      mapVerify(knows3bn, knows3bn, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(0), xbn(2) -> xbn(1))) should be(Nil)

      val asymmetric = knows3bn union Graph(Triple(xbn(0), foaf("name"), Literal("Tim")))
      mapVerify(asymmetric, asymmetric, Map(xbn(0) -> xbn(0), xbn(1) -> xbn(1), xbn(2) -> xbn(2))) should be(Nil)
      mapVerify(asymmetric, asymmetric, Map(xbn(0) -> xbn(1), xbn(1) -> xbn(2), xbn(2) -> xbn(0))) should not be empty
      mapVerify(asymmetric, asymmetric, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(0), xbn(2) -> xbn(1))) should not be empty

    }

  }

  "isomorphism tests" when {

    "a 1 triple ground graph" in {
      val g1 = (hjs -- foaf.name ->- "Henry Story").graph
      val expected = Graph(Triple(hjs, foaf.name, Literal("Henry Story")))
      findAnswer(g1, expected).isSuccess should be(true)

      val nonExpected = Graph(Triple(hjs, foaf.name, Literal("Henri Story")))
      findAnswer(g1, nonExpected).isSuccess should be(false)
    }

    "two grounded graphs with 2 relations" in {
      val g1 = groundedGraph
      val expected = groundedGraph
      findAnswer(g1, expected).isSuccess should be(true)
    }

    "list of size 1" in {
      val g = list(1, "h")
      val expected = list(1, "g")
      findAnswer(g, expected).isSuccess should be(true)
    }

    "list of size 2" in {
      val g = list(2, "h")
      val expected = list(2, "g")
      findAnswer(g, expected).isSuccess should be(true)
    }

    "list of size 5" in {
      val g = list(5, "h")
      val expected = list(5, "g")
      findAnswer(g, expected).isSuccess should be(true)
    }

  }


}
