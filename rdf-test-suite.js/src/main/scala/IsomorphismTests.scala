package org.w3.banana.util


import org.w3.banana.{RDF, RDFOps}

import scala.collection.immutable.ListMap
import scala.scalajs.test.JasmineTest
import scala.util.Success

abstract class IsomorphismTests[Rdf<:RDF]()(implicit ops: RDFOps[Rdf]) extends JasmineTest {

  import ops._
  import org.w3.banana.diesel._

  val graphIsomorphism = new GraphIsomporphism()(ops)
  import graphIsomorphism._


  def foaf(tag: String) = URI("http://xmlns.com/foaf/0.1/"+tag)

  val hjs = URI("http://bblfish.net/people/henry/card#me")

  val timbl = URI("http://www.w3.org/People/Berners-Lee/card#i")
  def alex(i: Int) = BNode("alex"+i)
  def antonio(i: Int) = BNode("antonio"+i)

  val groundedGraph = (
    toPointedGraphW[Rdf](hjs)
      -- foaf("knows") ->- timbl
      -- foaf("name") ->- "Henry Story"
    ).graph

  //  val bnodeGraph = (
  //      toPointedGraphW[Plantain](URI("#me"))
  //        -- foaf("knows") ->- toPointedGraphW[Plantain](bnode("alex"))
  //    ).graph union (
  //      toPointedGraphW[Plantain](bnode("alex"))
  //        -- foaf("name") ->- "Alexandre Bertails"
  //    ).graph


  def bnAlexRel1Graph(i: Int=1) = Graph(Triple(alex(i), foaf("homePage"), URI("http://bertails.org/")))

  def bnAlexRel2Graph(i: Int=1) = Graph(
    Triple(hjs, foaf("knows"), alex(i)),
    Triple(alex(i), foaf("name"), "Alexandre Bertails".toNode)
  )


  def bnAntonioRel1Graph(i: Int=1) = Graph(Triple(antonio(i), foaf("homePage"), URI("https://github.com/antoniogarrote/")))

  def bnAntonioRel2Graph(i: Int=1) = Graph(
    Triple(hjs, foaf("knows"), antonio(i)),
    Triple(antonio(i), foaf("name"), "Antonio Garrote".toNode)
  )

  def xbn(i: Int) = BNode("x"+i)

  def bnKnowsBN(i:Int, j: Int) = Graph(
    Triple(xbn(i),foaf("knows"),xbn(j))
  )

  def symmetricGraph(i: Int, j: Int) = bnKnowsBN(i,j) union bnKnowsBN(j,i)

  def owlSameAs(node1: Rdf#Node,node2: Rdf#Node) =
    Graph(Triple(node1,URI("http://www.w3.org/2002/07/owl#sameAs"),node2))


  describe("test groundTripleFilter(graph)") {

    it("a completely grounded graph ( no blank nodes ) ") {
      val (grounded, nongrounded) = groundTripleFilter(groundedGraph)
      grounded.toIterable foreach {
        triple => expect( groundedGraph.contains(triple) ).toEqual(true)
      }
      expect(nongrounded == emptyGraph).toEqual(true)
    }

    it("an ungrounded graph ( all statements contain a bnode )") {
      val (grounded, nongrounded) = groundTripleFilter(bnAlexRel2Graph())
      expect(grounded == emptyGraph).toEqual(true)
      expect(nongrounded == nongrounded).toEqual(true)
    }

    it("a graph with grounded and ungrounded statements ") {
      val (grounded, nongrounded) = groundTripleFilter(groundedGraph union bnAlexRel2Graph())
      expect(grounded == groundedGraph).toEqual(true)
      expect(nongrounded == nongrounded).toEqual(true)
    }

  }

  describe("test categorisation of bnodes") {

    it("one bnode with 1 relation") {
      val clz = bnodeClassify(bnAlexRel1Graph())
      expect(clz.size).toEqual(1)
      expect(clz.head._2.size).toEqual(1) // only one bnode in this graph
      expect(clz.head._1 == (new VerticeType(List((foaf("homePage"), 1)), List()))).toEqual(true)

      val clz2 = bnodeClassify(bnAntonioRel1Graph())
      expect(clz2.size).toEqual(1)
      expect(clz2.head._2.size).toEqual(1) // only one bnode in this graph
      expect(clz2.head._1 == (new VerticeType(List((foaf("homePage"), 1)), List()))).toEqual(true)

    }

    it("one bnode with 2 relations") {
      val clz = bnodeClassify(bnAlexRel2Graph())
      expect(clz.size).toEqual(1)
      expect(clz.head._2.size).toEqual(1) // only one bnode in this classification
      expect(clz.head._1 == (new VerticeType(List((foaf("name"), 1)), List((foaf("knows"), 1))))).toEqual(true)

      val clz2 = bnodeClassify(bnAntonioRel2Graph())
      expect(clz2.size).toEqual(1)
      expect(clz2.head._2.size).toEqual(1) // only one bnode in this classification
      expect(clz2.head._1 == (new VerticeType(List((foaf("name"), 1)), List((foaf("knows"), 1))))).toEqual(true)
    }

    it("one bnode with 3 relations") {
      val clz = bnodeClassify(bnAlexRel1Graph() union bnAlexRel2Graph())
      expect(clz.size).toEqual(1)
      expect(clz.head._2.size).toEqual(1) // only one bnode in this classification
      expect(clz.head._1 == (new VerticeType(List((foaf("name"), 1), (foaf("homePage"), 1)), List((foaf("knows"), 1))))).toEqual(true)

      val clz2 = bnodeClassify(bnAntonioRel1Graph() union bnAntonioRel2Graph())
      expect(clz2.size).toEqual(1)
      expect(clz2.head._2.size).toEqual(1) // only one bnode in this classification
      expect(clz2.head._1 == (new VerticeType(List((foaf("name"), 1), (foaf("homePage"), 1)), List((foaf("knows"), 1))))).toEqual(true)
    }

    it("two bnodes with each same type of relation") {
      val bnGr = bnAlexRel1Graph() union bnAntonioRel1Graph()
      val clz = bnodeClassify(bnGr)
      expect(clz.size).toEqual(1)
      expect(clz.head._2.size).toEqual(2) // 2 bnodes in this classification
      expect(clz.head._1 == (new VerticeType(List((foaf("homePage"), 1)), List()))).toEqual(true)
    }

    it("two bnodes with each 2 relations of same type") {
      val bnGr = bnAlexRel2Graph() union bnAntonioRel2Graph()
      val clz = bnodeClassify(bnGr)
      expect(clz.size).toEqual(1)
      expect(clz.head._2.size).toEqual(2) // 2 bnodes in this classification
      expect(clz.head._1 == (new VerticeType(List((foaf("name"), 1)), List((foaf("knows"),1))))).toEqual(true)
    }


  }

  describe("test bnode mapping solutions ") {
    it("two graphs with 1 relation and 1 bnode") {
      val maps = bnodeMappingGenerator(bnAlexRel1Graph(1),bnAlexRel1Graph(2))
      expect(maps == Success(ListMap(alex(1)->Set(alex(2))))).toEqual(true)
      val answer = findAnswer(bnAlexRel1Graph(1),bnAlexRel1Graph(2))
      expect(answer == Success(List(alex(1)->(alex(2))))).toEqual(true)
    }

    it("two graphs with 2 relation and 1 bnode each") {
      val maps = bnodeMappingGenerator(bnAlexRel2Graph(1),bnAlexRel2Graph(2))
      expect(maps == Success(ListMap(alex(1)->Set(alex(2))))).toEqual(true)
      val answer = findAnswer(bnAlexRel2Graph(1),bnAlexRel2Graph(2))
      expect(answer == Success(List(alex(1)->(alex(2))))).toEqual(true)
    }

    it("two graphs with 3 relations and 1 bnode each ") {
      val maps = bnodeMappingGenerator(
        bnAlexRel1Graph(1) union bnAlexRel2Graph(1),
        bnAlexRel1Graph(2) union bnAlexRel2Graph(2))
      expect(maps == Success(ListMap(alex(1)->Set(alex(2))))).toEqual(true)
      val answer = findAnswer(
        bnAlexRel1Graph(1) union bnAlexRel2Graph(1),
        bnAlexRel1Graph(2) union bnAlexRel2Graph(2))
      expect(answer == Success(List(alex(1)->(alex(2))))).toEqual(true)
    }

    it("two graphs with 2 relations and 2 bnodes each") {
      for (l <- findPossibleMappings(
        bnAlexRel1Graph(1) union bnAntonioRel1Graph(1),
        bnAlexRel1Graph(2) union bnAntonioRel1Graph(2))) {
        //with this system of categorisation the categories are very light
        // and they don't distinguish the literals
        //also the returned set covers symmetric results - this can also be optimised!
        expect(l.size).toEqual(4)
      }

      val answer = findAnswer(
        bnAlexRel1Graph(1) union bnAntonioRel1Graph(1),
        bnAlexRel1Graph(2) union bnAntonioRel1Graph(2)
      )
      expect(answer.isSuccess).toEqual(true)
      expect(answer.get.size).toEqual(2)
      expect(answer.get.contains(alex(1)->alex(2))).toEqual(true)
      expect(answer.get.contains(antonio(1)->antonio(2))).toEqual(true)
    }

    it("two graphs with 3 relations each. | But one category has 1 solution the other that has two. | The category with 1 solutions must be shown first") {
      val g1 = bnAlexRel1Graph(1) union bnAntonioRel1Graph(1) union bnAlexRel2Graph(2)    union bnAlexRel1Graph(0) union bnAlexRel2Graph(0)
      val g2 = bnAlexRel1Graph(3) union bnAntonioRel1Graph(3) union bnAntonioRel2Graph(4) union bnAlexRel1Graph(5) union bnAlexRel2Graph(5)
      val answers = findPossibleMappings(g1,g2)
      val answer= findAnswer(g1,g2)
      expect(answer.isFailure).toEqual(true)

    }
  }

  describe("test bnode mapping") {

    it("graphs mapped to themselves") {
      val a1g = bnAlexRel1Graph(0)
      expect(mapVerify(a1g, a1g, Map(alex(0) -> alex(0))) == Nil).toEqual(true)

      expect(mapVerify(bnAntonioRel1Graph(0), bnAntonioRel1Graph(0), Map((antonio(0), antonio(0)))) == Nil).toEqual(true)

    }

    it("1 bnode mapped") {
      expect(mapVerify(bnAlexRel1Graph(0), bnAlexRel1Graph(1), Map((alex(0), alex(1)))) == Nil).toEqual(true)
      expect(mapVerify(bnAntonioRel2Graph(0), bnAntonioRel1Graph(1), Map((antonio(0), antonio(1)))).isEmpty).toEqual(false)
    }

    it("2 bnodes mapped") {
      val r2g1 = bnAlexRel1Graph(0) union bnAntonioRel1Graph(0)
      val r2g2 = bnAlexRel1Graph(1) union bnAntonioRel1Graph(1)

      mapVerify(r2g1, r2g1, Map(alex(0) -> alex(0), antonio(0) -> antonio(0)))

      expect(mapVerify(
        r2g1,
        r2g2,
        Map(alex(0) -> alex(1), antonio(0) -> antonio(1))
      ) == Nil).toEqual(true)

      //an incorrect mapping
      val v = mapVerify(
        r2g1,
        r2g2,
        Map(alex(0) -> antonio(1), antonio(0) -> alex(1))
      )
      expect(v.isEmpty).toEqual(false)

      //reverse test
      expect(mapVerify(
        r2g2,
        r2g1,
        Map(alex(1) -> alex(0), antonio(1) -> antonio(0))
      ) == Nil).toEqual(true)

      //an incorrect mapping
      val v2 = mapVerify(
        r2g2,
        r2g1,
        Map(alex(1) -> antonio(0), antonio(1) -> alex(0))
      )
      expect(v2.isEmpty).toEqual(false)

    }

    it("some symmetric graphs can have more than one mapping - which are thus isomorphic") {

      //some graphs have two mappings
      val symgrph01 = symmetricGraph(0, 1)
      val symgrph23 = symmetricGraph(2, 3)
      expect(mapVerify(symgrph01, symgrph23, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) == Nil).toEqual(true)
      expect(mapVerify(symgrph01, symgrph23, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) == Nil).toEqual(true)

      val symgraph01ext = symgrph01 union owlSameAs(xbn(0), xbn(0)) union owlSameAs(xbn(1), xbn(1))
      val symgraph23ext = symgrph23 union owlSameAs(xbn(2), xbn(2)) union owlSameAs(xbn(3), xbn(3))
      expect(mapVerify(symgraph01ext, symgraph23ext, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) == Nil).toEqual(true)
      expect(mapVerify(symgraph01ext, symgraph23ext, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) == Nil).toEqual(true)

      val oneThing01 = symgraph01ext union owlSameAs(xbn(0), xbn(1)) union owlSameAs(xbn(1), xbn(0))
      val oneThing23 = symgraph23ext union owlSameAs(xbn(2), xbn(3)) union owlSameAs(xbn(3), xbn(2))
      expect(mapVerify(oneThing01, oneThing23, Map(xbn(0) -> xbn(2), xbn(1) -> xbn(3))) == Nil).toEqual(true)
      expect(mapVerify(oneThing01, oneThing23, Map(xbn(0) -> xbn(3), xbn(1) -> xbn(2))) == Nil).toEqual(true)
    }

    it("3 bnodes mapped") {
      val knows3bn = bnKnowsBN(0,1) union bnKnowsBN(1,2) union bnKnowsBN(2,0)

      //three different isomorphic mappings
      expect(mapVerify(knows3bn,knows3bn,Map(xbn(0)->xbn(0),xbn(1)->xbn(1),xbn(2)->xbn(2))) == Nil).toEqual(true)
      expect(mapVerify(knows3bn,knows3bn,Map(xbn(0)->xbn(1),xbn(1)->xbn(2),xbn(2)->xbn(0))) == Nil).toEqual(true)
      expect(mapVerify(knows3bn,knows3bn,Map(xbn(0)->xbn(2),xbn(1)->xbn(0),xbn(2)->xbn(1))) == Nil).toEqual(true)

      val asymmetric = knows3bn union Graph(Triple(xbn(0),foaf("name"),Literal("Tim")))
      expect(mapVerify(asymmetric,asymmetric,Map(xbn(0)->xbn(0),xbn(1)->xbn(1),xbn(2)->xbn(2))) == Nil).toEqual(true)
      expect(mapVerify(asymmetric,asymmetric,Map(xbn(0)->xbn(1),xbn(1)->xbn(2),xbn(2)->xbn(0))).isEmpty).toEqual(false)
      expect(mapVerify(asymmetric,asymmetric,Map(xbn(0)->xbn(2),xbn(1)->xbn(0),xbn(2)->xbn(1))).isEmpty).toEqual(false)

    }
  }

}
