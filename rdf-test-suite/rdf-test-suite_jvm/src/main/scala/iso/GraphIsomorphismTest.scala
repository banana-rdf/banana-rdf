package org.w3.banana.plantain.iso

import org.scalatest.{ Matchers, Suite, WordSpec }
import org.w3.banana.iso.{ GraphIsomorphism, IsomorphismBNodeTrait, VerticeCBuilder }
import org.w3.banana.{ RDF, RDFOps }

import scala.collection.immutable.ListMap
import scala.util.{ Failure, Success }

/**
 * Tests for the pure Scala implementation of Graph Isomorphism
 */
class GraphIsomorphismTest[Rdf <: RDF](isoFactory: (() => VerticeCBuilder[Rdf]) => GraphIsomorphism[Rdf])(
  implicit val ops: RDFOps[Rdf])
    extends WordSpec with IsomorphismBNodeTrait[Rdf] with Matchers { self: Suite =>

  import ops._
  import org.w3.banana.diesel._
  import org.w3.banana.iso.MappingGenerator._

  val countingIso = isoFactory(VerticeCBuilder.counting)
  val simpleHashIso = isoFactory(VerticeCBuilder.simpleHash)

  "test groundTripleFilter(graph)" when {
    import countingIso._

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
    import countingIso._

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

    "two grounded graphs with 4 relations" in {
      val g1 = Graph(Triple(hjs, foaf.lastName, Literal("Story")),
        Triple(hjs, foaf.interest, Literal("philosophy")),
        Triple(hjs, foaf.interest, Literal("category theory"))) union groundedGraph

      val expected = groundedGraph +
        Triple(hjs, foaf.interest, Literal("philosophy")) +
        Triple(hjs, foaf.lastName, Literal("Story")) +
        Triple(hjs, foaf.interest, Literal("category theory"))

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
    import countingIso._

    "graphs mapped to themselves" in {
      val a1g = bnAlexRel1Graph(0)
      mapVerify(a1g, a1g, List(alex(0) -> alex(0))) should be(Nil)

      mapVerify(bnAntonioRel1Graph(0), bnAntonioRel1Graph(0), List((antonio(0), antonio(0)))) should be(Nil)

    }

    "1 bnode mapped" in {
      mapVerify(bnAlexRel1Graph(0), bnAlexRel1Graph(1), List((alex(0), alex(1)))) should be(Nil)

      mapVerify(bnAntonioRel2Graph(0), bnAntonioRel1Graph(1), List((antonio(0), antonio(1)))) should not be empty
    }

    "2 bnodes mapped" in {
      val r2g1 = bnAlexRel1Graph(0) union bnAntonioRel1Graph(0)
      val r2g2 = bnAlexRel1Graph(1) union bnAntonioRel1Graph(1)

      mapVerify(r2g1, r2g1, List(alex(0) -> alex(0), antonio(0) -> antonio(0)))

      mapVerify(
        r2g1,
        r2g2,
        List(alex(0) -> alex(1), antonio(0) -> antonio(1))) should be(Nil)

      //an incorrect mapping
      val v = mapVerify(
        r2g1,
        r2g2,
        List(alex(0) -> antonio(1), antonio(0) -> alex(1)))
      v should not be empty

      //reverse test

      mapVerify(
        r2g2,
        r2g1,
        List(alex(1) -> alex(0), antonio(1) -> antonio(0))) should be(Nil)

      //an incorrect mapping
      val v2 = mapVerify(
        r2g2,
        r2g1,
        List(alex(1) -> antonio(0), antonio(1) -> alex(0)))
      v2 should not be empty

    }

    "list of size 1" in {
      val g1 = list(1, "h")
      val g2 = list(1, "g")

      mapVerify(g1, g2, List(BNode("h1") -> BNode("g1"), BNode("h0") -> BNode("g0"))) should be(Nil)
    }

    "list of size 2" in {
      val g1 = list(2, "h")
      val g2 = list(2, "g")

      mapVerify(g1, g2, List(
        BNode("h2") -> BNode("g2"),
        BNode("h1") -> BNode("g1"),
        BNode("h0") -> BNode("g0"))) should be(Nil)
    }

    "list of size 5" in {
      val g1 = list(5, "h")
      val g2 = list(5, "g")

      mapVerify(g1, g2, List(
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
      mapVerify(symgrph01, symgrph23, List(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(symgrph01, symgrph23, List(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)

      val symgraph01ext = symgrph01 union owlSameAs(xbn(0), xbn(0)) union owlSameAs(xbn(1), xbn(1))
      val symgraph23ext = symgrph23 union owlSameAs(xbn(2), xbn(2)) union owlSameAs(xbn(3), xbn(3))
      mapVerify(symgraph01ext, symgraph23ext, List(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(symgraph01ext, symgraph23ext, List(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)

      val oneThing01 = symgraph01ext union owlSameAs(xbn(0), xbn(1)) union owlSameAs(xbn(1), xbn(0))
      val oneThing23 = symgraph23ext union owlSameAs(xbn(2), xbn(3)) union owlSameAs(xbn(3), xbn(2))
      mapVerify(oneThing01, oneThing23, List(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) should be(Nil)
      mapVerify(oneThing01, oneThing23, List(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) should be(Nil)
    }

    "3 bnodes mapped" in {
      val knows3bn = bnKnowsBN(0, 1) union bnKnowsBN(1, 2) union bnKnowsBN(2, 0)

      //three different isomorphic mappings
      mapVerify(knows3bn, knows3bn, List(xbn(0) -> xbn(0), xbn(1) -> xbn(1), xbn(2) -> xbn(2))) should be(Nil)
      mapVerify(knows3bn, knows3bn, List(xbn(0) -> xbn(1), xbn(1) -> xbn(2), xbn(2) -> xbn(0))) should be(Nil)
      mapVerify(knows3bn, knows3bn, List(xbn(0) -> xbn(2), xbn(1) -> xbn(0), xbn(2) -> xbn(1))) should be(Nil)

      val asymmetric = knows3bn union Graph(Triple(xbn(0), foaf("name"), Literal("Tim")))
      mapVerify(asymmetric, asymmetric, List(xbn(0) -> xbn(0), xbn(1) -> xbn(1), xbn(2) -> xbn(2))) should be(Nil)
      mapVerify(asymmetric, asymmetric, List(xbn(0) -> xbn(1), xbn(1) -> xbn(2), xbn(2) -> xbn(0))) should not be empty
      mapVerify(asymmetric, asymmetric, List(xbn(0) -> xbn(2), xbn(1) -> xbn(0), xbn(2) -> xbn(1))) should not be empty

    }

  }

  "isomorphism tests" when {
    import countingIso._

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

    "list of size 5 with simple hashIso" in {
      import simpleHashIso._
      val g = list(5, "h")
      val expected = list(5, "g")
      findAnswer(g, expected).isSuccess should be(true)
    }

  }

  "tree of possibilities" when {

    //we use integers here to test as these are easier to work with and the code is generic anyway
    val lm = ListMap(1 -> Set(1))

    "for a map with 1 node mapped to one node" in {
      complexity(Success(lm)) should be(1)
      branches(treeLevels(lm)).size should be(1)
    }

    val lmX = lm ++ ListMap(2 -> Set(1))

    "complexity calculation for bad maps" in {
      complexity(Failure(new Error("xxx"))) should be(0)
      complexity(Success(lmX)) should be(1) // the bad maps still count as a solution to look at
      branches(treeLevels(lmX)).size should be(1)
    }

    "complexity calc for 3 bnodes size" in {
      val lm3 = ListMap(1 -> Set(2, 3)) ++ ListMap(2 -> Set(22, 23)) ++ ListMap(3 -> Set(32, 33, 34))
      complexity(Success(lm3)) should be(12) // the bad maps still count as a solution to look at
      val branchstream = branches(treeLevels(lm3))
      //todo: how can one test that a stream is lazy?
      branchstream.toList.size should be(12)
    }
  }

  "larger graphs" when {

    val g1 = list(5, "i") union list(3, "j")
    val g2 = list(5, "g") union list(3, "h")

    "complexity analysis for counting iso" in {
      val lm = countingIso.mappingGen.bnodeMappings(g1, g2)
      complexity(lm) should be(67108864)
    }

    "complexity analysis for simple hash iso" in {
      val lm = simpleHashIso.mappingGen.bnodeMappings(g1, g2)
      complexity(lm) should be(64) //<-- 1 million time improvement over simpleHash
    }

    "counting Iso is too slow, but fails gracefully without exploring all the possibilities" in {
      countingIso.findAnswer(g1, g2).isSuccess should be(false)
    }

    "SimpleHash Iso has no trouble finding results" in {
      val answer = simpleHashIso.findAnswer(g1, g2)
      answer.isSuccess should be(true)
    }

  }

}
