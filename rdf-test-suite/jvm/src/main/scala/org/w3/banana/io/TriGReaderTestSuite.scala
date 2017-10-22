package org.w3.banana.io

import java.io._

import org.scalatest._
import org.w3.banana._
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import scala.util._

/**
 *
 */
class TriGReaderTestSuite[Rdf <: RDF](implicit
                                      ops: RDFOps[Rdf],
                                      reader: RDFQuadReader[Rdf, Try, TriG]
) extends WordSpec with Matchers {

  import ops._

  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val foaf = FOAFPrefix[Rdf]
  val dc = DCTPrefix[Rdf]


  val typ = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

  def exvuri(n: String): Rdf#URI = URI("http://www.example.org/vocabulary#" + n)
  def exuri(n: String): Rdf#URI = URI("http://example.org/" + n)


  /**
    * taken from https://www.w3.org/TR/trig/#sec-graph-statements
    * This document encodes one graph.
    */
  val trig_example1: String =
    """
      |@prefix ex: <http://www.example.org/vocabulary#> .
      |@prefix : <http://www.example.org/exampleDocument#> .
      |
      |:G1 { :Monica a ex:Person ;
      |              ex:name "Monica Murphy" ;
      |              ex:homepage <http://www.monicamurphy.org> ;
      |              ex:email <mailto:monica@monicamurphy.org> ;
      |              ex:hasSkill ex:Management ,
      |                          ex:Programming . }
    """.stripMargin

  val trig_graph1 = (URI("http://www.example.org/exampleDocument#Monica").a(exvuri("Person"))
    -- exvuri("name") ->- "Monica Murphy"
    -- exvuri("homepage") ->- URI("http://www.monicamurphy.org")
    -- exvuri("email") ->- URI("mailto:monica@monicamurphy.org")
    -- exvuri("hasSkill") ->- exvuri("Management")
    -- exvuri("hasSkill") ->- exvuri("Programming")
  ).graph

  /**
    * taken from https://www.w3.org/TR/trig/#sec-graph-statements
    * This document contains a default graph and two named graphs.
    */
  val trig_example2: String =
    """
      |
      |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
      |@prefix dc: <http://purl.org/dc/terms/> .
      |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
      |
      |# default graph
      |    {
      |      <http://example.org/bob> dc:publisher "Bob" .
      |      <http://example.org/alice> dc:publisher "Alice" .
      |    }
      |
      |<http://example.org/bob>
      |    {
      |       _:a foaf:name "Bob" .
      |       _:a foaf:mbox <mailto:bob@oldcorp.example.org> .
      |       _:a foaf:knows _:b .
      |    }
      |
      |<http://example.org/alice>
      |    {
      |       _:b foaf:name "Alice" .
      |       _:b foaf:mbox <mailto:alice@work.example.org> .
      |    }
      |
    """.stripMargin

  val trig_graph2_default = Graph(
    Triple(exuri("bob"), dc.apply("publisher"), Literal("Bob")),
    Triple(exuri("alice"), dc.apply("publisher"), Literal("Alice"))
  )

  val trig_graph2_bob = Graph(
    Triple(BNode("a"), foaf.name, Literal("Bob")),
    Triple(BNode("a"), foaf.mbox, URI("mailto:bob@oldcorp.example.org")),
    Triple(BNode("a"), foaf.knows, BNode("b"))
  )

  val trig_graph2_alice = Graph(
    Triple(BNode("b"), foaf.name, Literal("Alice")),
    Triple(BNode("b"), foaf.mbox, URI("mailto:alice@work.example.org"))
  )

  /**
    * taken from https://www.w3.org/TR/trig/#sec-graph-statements
    * This document contains a default graph and two named graphs.
    */
  val trig_example3: String =
    """
      |
      |@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
      |@prefix dc: <http://purl.org/dc/terms/> .
      |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
      |
      |# default graph - no {} used.
      |<http://example.org/bob> dc:publisher "Bob" .
      |<http://example.org/alice> dc:publisher "Alice" .
      |
      |# GRAPH keyword to highlight a named graph
      |# Abbreviation of triples using ;
      |GRAPH <http://example.org/bob>
      |{
      |   [] foaf:name "Bob" ;
      |      foaf:mbox <mailto:bob@oldcorp.example.org> ;
      |      foaf:knows _:b .
      |}
      |
      |GRAPH <http://example.org/alice>
      |{
      |    _:b foaf:name "Alice" ;
      |        foaf:mbox <mailto:alice@work.example.org>
      |}
    """.stripMargin

  val trig_example_bnode =
    """
      |@prefix : <http://example.org/> .
      |@prefix dc: <http://purl.org/dc/terms/> .
      |@prefix foaf: <http://xmlns.com/foaf/0.1/> .
      |
      |
      |:bob dc:publisher _:a .
      |
      |_:a {
      |   :bob    foaf:knows :alice .
      |   :alice  foaf:name "Alice" ;
      |           dc:publisher _:b .
      |}
      |
      |_:b {
      |   :alice  foaf:mbox <mailto:alice@work.example.org> .
      |}
      |
    """.stripMargin

  "test that the parser can parse single components. Parser " should {

    "parse default graph " in {
      val graph = reader.readDefaultGraph(new StringReader(trig_example3), rdfCore).get
      println(graph)
      graph isIsomorphicWith trig_graph2_default shouldEqual true
    }

    "parse all named graphs to one graph" in {
      val graph = reader.read(new StringReader(trig_example1), rdfCore).get
      graph isIsomorphicWith trig_graph1 shouldEqual true
    }

    "parse specific named graph" in {
      val graph = reader.read((new StringReader(trig_example2)), rdfCore, URI("http://example.org/bob")).get
      graph isIsomorphicWith trig_graph2_bob shouldEqual true
    }

    "parse multiple named graphs" in {
      val graphs = reader.readAll(new StringReader(trig_example2), rdfCore).get
      graphs.size shouldEqual 3
      graphs.keySet.contains(None) && graphs.keySet.contains(Some(exuri("bob"))) && graphs.keySet.contains(Some(exuri("alice"))) shouldEqual true
    }


    "parse multiple named graphs in with GRAPH keyword " in {
      val graphs = reader.readAll(new StringReader(trig_example3), rdfCore).get
      println("graphs: " + graphs.keys.mkString(" , "))
      graphs.get(None) shouldBe defined
      graphs(None) isIsomorphicWith trig_graph2_default shouldEqual true
      graphs.get(Some(URI("http://example.org/bob"))) shouldBe defined
      graphs(Some(URI("http://example.org/bob"))) isIsomorphicWith trig_graph2_bob shouldEqual true
      graphs.get(Some(URI("http://example.org/alice"))) shouldBe defined
      graphs(Some(URI("http://example.org/alice"))) isIsomorphicWith trig_graph2_alice shouldEqual true
    }

    "parse multiple named graphs, named with blank nodes " in {
      val graphs = reader.readAll(new StringReader(trig_example_bnode), rdfCore).get
      println("graphs: " + graphs.keys.mkString(" , "))
      graphs.keys.size shouldEqual 3

      val graphAPG = PointedGraph(exuri("bob"), graphs(None)) / dc.apply("publisher")
      graphAPG.takeOnePointedGraph.isSuccess shouldBe true
      graphs.get(Some(graphAPG.takeOnePointedGraph.get.pointer)) shouldBe defined

      val graphBPG = PointedGraph(exuri("alice"), graphs(Some(graphAPG.takeOnePointedGraph.get.pointer)) ) / dc.apply("publisher")
      graphBPG.takeOnePointedGraph.isSuccess shouldBe true
      graphs.get(Some(graphBPG.takeOnePointedGraph.get.pointer)) shouldBe defined

      (PointedGraph(exuri("alice"), graphs(Some(graphBPG.takeOnePointedGraph.get.pointer))) / foaf.mbox).exactlyOneAs[Rdf#URI] shouldEqual Success(URI("mailto:alice@work.example.org"))


    }

  }

}

