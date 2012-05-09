///*
// * Copyright (c) 2012 Henry Story
// * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
// */
//
//package org.w3.banana.n3
//
//
//import org.junit.Test
//import org.junit.Assert._
//import util.Random
//import java.io._
//import _root_.nomo.Accumulator
//import com.hp.hpl.jena.rdf.model.{ModelFactory=>JenaModelFactory, Model => JenaModel}
//import org.w3.banana._
//import jena.{Jena, JenaOperations}
//import sesame.{Sesame, SesameOperations, SesameTurtleReader, SesameGraphIsomorphism}
//
//
// TODO make this more generic.
// TODO: the difficulty will be to find an asynchronous api to test the asynchronous reader too
///**
// * This is a class that enables each implementation of org.w3.banana to test its ability to
// * interact with the NTriples parser written with Nomo.
// *
// * The test suites in the test directory test Nomo versus the parsers from Sesame, Jena, etc...
// *
// * @param ops
// * @param parser
// * @tparam Rdf
// * @tparam F
// * @tparam E
// * @tparam X
// */
//abstract class NTriplesParserTest[Rdf <: RDF, F, E, X](
//    val ops: RDFOperations[Rdf],
//    val parser: NTriplesParser[Rdf, F, E, X, Listener[Rdf]]) {
//
//  val isomorphism: GraphIsomorphism[Rdf]
//
//  import ops._
//  import isomorphism._
//
//  implicit def U = new Listener(ops)
//
//  /** so that this test can be run with different IO models */
//  def toF(string: String): F
//
//  val n3 ="""
//  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett" .
//  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Art Barstow" .
//
// <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
//
// """
//
//
//
//  def randomSz = {
//    val random = 29 to 47+1
//    random(Random.nextInt(random.length ))
//  }
//
//
//  @Test()
//  def read_simple_n3(): Unit = {
//    val res = parser.nTriples(n3)
//    val tr = res.user.queue.toList.map(_.asInstanceOf[Triple])
//    val parsedGraph = Graph(tr)
//    assertEquals("should be three triples in graph",3,parsedGraph.size)
//
//    val ntriples = IRI("http://www.w3.org/2001/sw/RDFCore/ntriples/")
//    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
//    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
//    val dave = "Dave Beckett".§
//    val art = "Art Barstow".§
//    val w3org = IRI("http://www.w3.org/")
//
//    val expected =
//      Graph(
//        (ntriples, creator, dave),
//        (ntriples, creator, art),
//        (ntriples, publisher, w3org)
//      )
//
//    assertTrue("graphs must be isomorphic", expected isIsomorphicWith parsedGraph)
//  }
//
//  private val mToJena = new RDFTransformer[Rdf, Jena](ops, JenaOperations)
//  private val mToSesame = new RDFTransformer[Rdf, Sesame](ops, SesameOperations)
//
//  @Test
//  def official_n3_test {
//    val testFile =  new File(this.getClass.getResource("/www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt").toURI)
//
//    //read using Jena's NTriples parser, so we can test the n3 NTriples parser by comparing outputs
//    val jenaModel = JenaModelFactory.createDefaultModel()
//    jenaModel.read(new FileInputStream(testFile),null,"N-TRIPLE")
//    val jenaGraph = jenaModel.getGraph
//    assertEquals("There were read 30 triples in "+testFile.getPath,30,jenaModel.size())
//
//
//    val sesameReader = SesameTurtleReader.read(testFile,"")
//    assertTrue("sesame parsed document",sesameReader.isRight)
//    assertEquals("there were 30 triples in the "+testFile.getPath,30,sesameReader.right.get.size())
//
//    var chunk = ParsedChunk(parser.nTriples,parser.P.annotator(U))
//    var inOpen = true
//    val in = new FileInputStream(testFile)
//    val bytes = new Array[Byte](randomSz)
//
//    while (inOpen ) {
//      try {
//        val length = in.read(bytes)
//        if (length > -1) {
//          chunk = chunk.parse(new String(bytes, 0, length, "ASCII"))
//        } else inOpen = false
//      } catch {
//        case e: IOException => inOpen = false
//      }
//    }
//
//    val result = chunk.parser.result(chunk.acc)
//    val res = result.user.queue.toList.map(_.asInstanceOf[Triple])
//    val g = Graph(res)
//    assertEquals("Our parser should have read 30 triples"+testFile.getPath,30,res.size)
//
//    val gAsSesame = mToSesame.transform(g)
//    assertTrue("the two graphs must be isomorphic (using Sesame isomorphism comparison)",
//      SesameGraphIsomorphism.isomorphism (gAsSesame, sesameReader.right.get) )
//
//    //first we look that each triple can be found (to narrow down issues)
//    res.foreach {
//      var counter = 0
//      triple =>
//        counter += 1
//      def bn(node: Node): Jena#Node = node match { case _ :BNode => null; case other=>mToJena.transformNode(other) }
//
//      //the following seems to indicate that a map function for a triple would be useful.
//      //something that would allow us to do triple.map(bn(_)) and then pass that on
//      val res = jenaGraph.find( bn(triple.subject),bn(triple.predicate),bn(triple.objectt) )
//
//      assertTrue("the jena graph must contain every statement we read with our parser. \r\n" +
//        "Testing triple no "+ counter +": "+triple, res.hasNext)
//    }
//
//    //this fails. Not surew here the fault lies.
//    val gAsJena = mToJena.transform(g)
//    assertTrue("the two graphs must be isomorphic (using Jena's isomorphism comparison)",
//      gAsJena isIsomorphicWith jenaGraph)
//  }
//
//
//  @Test()
//  def read_long_n3s_in_chunks() {
//
//    val card: File = new File(this.getClass.getResource("/card.nt").toURI)
//    val in = new FileInputStream(card)
//    val bytes = new Array[Byte](randomSz)
//
//    val card_random: File = new File(this.getClass.getResource("/card.random.nt").toURI)
//    val inR = new FileInputStream(card_random)
//    val bytesR = new Array[Byte](randomSz)
//
//    val jenaCard = JenaModelFactory.createDefaultModel()
//    jenaCard.read(new FileInputStream(card),null,"N-TRIPLE")
//    assertEquals("Pure Jena should have read 354 triples in"+card.getPath,354,jenaCard.size())
//
//    var chunk = ParsedChunk(parser.nTriples,parser.P.annotator(U))
//    var chunkR = ParsedChunk(parser.nTriples,parser.P.annotator(U))
//
//    //scala.io.Reader seemed to have a problem.
//    //here we test Asynchronous IO, feeding in pieces at a time
//    var inOpen = true
//    var inROpen = true
//    while (inOpen || inROpen) {
//      if (inOpen) {
//        try {
//          val length = in.read(bytes)
//          if (length > -1) {
//            chunk = chunk.parse(new String(bytes, 0, length, "ASCII"))
//          } else inOpen = false
//        } catch {
//          case e: IOException => inOpen = false
//        }
//      }
//      if (inROpen) {
//        try {
//          val length = inR.read(bytesR)
//          if (length > -1) {
//            chunkR = chunkR.parse(new String(bytesR, 0, length, "ASCII"))
//          } else inROpen = false
//        } catch {
//          case e: IOException => inROpen = false
//        }
//      }
//    }
//
//
//
//    val result = chunk.parser.result(chunk.acc)
//    val resultR = chunkR.parser.result(chunkR.acc)
//
//    val res = result.user.queue.toList.map(_.asInstanceOf[Triple])
//    val resR = resultR.user.queue.toList.map(_.asInstanceOf[Triple])
//
//
////    println("the last triple found was in card.nt was "+res.last)
////    println("the last triple found was in card.random.nt was "+resR.last)
//
//    assertNotSame("the results of reading both cards should be different lists",res,resR)
//
//    assertTrue("error parsing card.nt - failed at "+result.position+" status="+result.status,result.isSuccess)
//    assertTrue("error parsing card.random.nt - failed at "+resultR.position+" with status "+resultR.status,resultR.isSuccess)
//
//
//    val g = Graph(res)
//    val gR = Graph(resR)
//
//    assertEquals("There should be 354 triples in "+card.getPath,354,g.size)
//    assertEquals("There should be 354 triples in "+card_random.getPath,354,gR.size)
//
//    assertTrue("the two graphs must be isomorphic", g isIsomorphicWith gR)
//
//  }
//
//  import parser.P._
//  case class ParsedChunk(
//      val parser: Parser[Unit],
//      val acc: Accumulator[Char, X, Listener[Rdf]]) {
//    def parse(buf: Seq[Char]) = {
//      if (!buf.isEmpty) {
//        val (tripleParser, newAccu) = parser.feedChunked(buf, acc, buf.size)
//        ParsedChunk(tripleParser, newAccu)
//      } else {
//        this
//      }
//    }
//  }
//
//
//}