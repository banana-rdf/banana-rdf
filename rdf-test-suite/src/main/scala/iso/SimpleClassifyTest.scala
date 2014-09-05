package org.w3.banana.rdf.iso

import org.scalatest.{Suite, Matchers, WordSpec}
import org.w3.banana.iso.{IsomorphismBNodeTrait, SimpleMappingGenerator}
import org.w3.banana.{RDF, RDFOps}

import scala.collection.immutable.ListMap
import scala.util.Success

/**
 * Test the simple classification of Bnodes for the scala implementation of Isomorphism
 * Created by hjs on 04/09/2014.
 */
class SimpleClassifyTest[Rdf <: RDF](mappingGenerator: SimpleMappingGenerator[Rdf])(
  implicit val ops: RDFOps[Rdf])
  extends  WordSpec with IsomorphismBNodeTrait[Rdf] with Matchers  { self: Suite =>

  import mappingGenerator._
  import ops._

  "test categorisation of bnodes" when {

    "one bnode with 1 relation" in {
      val clz = bnodeClassify(bnAlexRel1Graph())
      clz.size should be(1)
      clz.head._2.size should be(1) // only one bnode in this graph
      clz.head._1 should be(new VerticeType(List((foaf.homepage, 1)), List()))

      val clz2 = bnodeClassify(bnAntonioRel1Graph())
      clz2.size should be(1)
      clz2.head._2.size should be(1) // only one bnode in this graph
      clz2.head._1 should be(new VerticeType(List((foaf.homepage, 1)), List()))

    }

    "one bnode with 2 relations" in {
      val clz = bnodeClassify(bnAlexRel2Graph())
      clz.size should be(1)
      clz.head._2.size should be(1) // only one bnode in this classification
      clz.head._1 should be(new VerticeType(List((foaf.name, 1)), List((foaf.knows, 1))))

      val clz2 = bnodeClassify(bnAntonioRel2Graph())
      clz2.size should be(1)
      clz2.head._2.size should be(1) // only one bnode in this classification
      clz2.head._1 should be(new VerticeType(List((foaf.name, 1)), List((foaf.knows, 1))))
    }

    "one bnode with 3 relations" in {
      val clz = bnodeClassify(bnAlexRel1Graph() union bnAlexRel2Graph())
      clz.size should be(1)
      clz.head._2.size should be(1) // only one bnode in this classification
      clz.head._1 should be(new VerticeType(List((foaf.name, 1), (foaf.homepage, 1)), List((foaf.knows, 1))))

      val clz2 = bnodeClassify(bnAntonioRel1Graph() union bnAntonioRel2Graph())
      clz2.size should be(1)
      clz2.head._2.size should be(1) // only one bnode in this classification
      clz2.head._1 should be(new VerticeType(List((foaf.name, 1), (foaf.homepage, 1)), List((foaf.knows, 1))))
    }

    "two bnodes with each same type of relation" in {
      val bnGr = bnAlexRel1Graph() union bnAntonioRel1Graph()
      val clz = bnodeClassify(bnGr)
      clz.size should be(1)
      clz.head._2.size should be(2) // 2 bnodes in this classification
      clz.head._1 should be(new VerticeType(List((foaf("homepage"), 1)), List()))
    }

    "two bnodes with each 2 relations of same type" in {
      val bnGr = bnAlexRel2Graph() union bnAntonioRel2Graph()
      val clz = bnodeClassify(bnGr)
      clz.size should be(1)
      clz.head._2.size should be(2) // 2 bnodes in this classification
      clz.head._1 should be(new VerticeType(List((foaf("name"), 1)), List((foaf("knows"), 1))))
    }

  }

  "bnode mapping generator" when {

    "two graphs with 1 relation and 1 bnode" in {
      val maps = bnodeMappingGenerator(bnAlexRel1Graph(1), bnAlexRel1Graph(2))
      maps should equal(Success(ListMap(alex(1) -> Set(alex(2)))))
//      val answer = findAnswer(bnAlexRel1Graph(1), bnAlexRel1Graph(2))
//      answer should equal(Success(List(alex(1) -> (alex(2)))))
    }

    "two graphs with 2 relation and 1 bnode each" in {
      val maps = bnodeMappingGenerator(bnAlexRel2Graph(1), bnAlexRel2Graph(2))
      maps should equal(Success(ListMap(alex(1) -> Set(alex(2)))))
//      val answer = findAnswer(bnAlexRel2Graph(1), bnAlexRel2Graph(2))
//      answer should equal(Success(List(alex(1) -> (alex(2)))))
    }

    "two graphs with 3 relations and 1 bnode each " in {
      val maps = bnodeMappingGenerator(
        bnAlexRel1Graph(1) union bnAlexRel2Graph(1),
        bnAlexRel1Graph(2) union bnAlexRel2Graph(2))
      maps should equal(Success(ListMap(alex(1) -> Set(alex(2)))))
//      val answer = findAnswer(
//        bnAlexRel1Graph(1) union bnAlexRel2Graph(1),
//        bnAlexRel1Graph(2) union bnAlexRel2Graph(2))
//      answer should equal(Success(List(alex(1) -> (alex(2)))))
    }

  }
}
