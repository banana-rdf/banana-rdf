package org.w3.rdf.n3

import org.junit.Test
import org.junit.Assert._
import util.Random
import java.io._
import nomo.{Success, Accumulator}
import com.hp.hpl.jena.rdf.model.{ModelFactory=>JenaModelFactory, Model => JenaModel}
import collection.mutable
import org.w3.rdf._

// would be happy to use
// NTriplesParserTest[M <: Model](m: M, parser: NTriplesParser[m.type], isomorphism: GraphIsomorphism[m.type])
// but the compiler complains, saying it does not know m
abstract class ParserTest[M <: RDFModule, F, E, X](val m: M, val parser: Parser[M, F, E, X, Listener]) {

  val isomorphism: GraphIsomorphism[m.type]
  
  import m._
  import isomorphism._
  
  implicit def U: Listener = new Listener
  
  /** so that this test can be run with different IO models */
  def toF(string: String): F

  val n3 ="""
  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett" .
  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Art Barstow" .

 <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .

 """

  def randomSz = {
    val random = 29 to 47+1
    random(Random.nextInt(random.length ))
  }

  
  @Test()
  def read_simple_n3(): Unit = {

    val res = parser.ntriples(n3)
    val tr = res.user.queue.toList.map(_.asInstanceOf[Triple])
    val parsedGraph = m.Graph(tr)
    assertEquals("should be three triples in graph",3,parsedGraph.size)

    val ntriples = IRI("http://www.w3.org/2001/sw/RDFCore/ntriples/")
    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
    val dave = "Dave Beckett".typedLiteral
    val art = "Art Barstow".typedLiteral
    val w3org = IRI("http://www.w3.org/")
    
    val expected = 
      Graph(
        (ntriples, creator, dave),
        (ntriples, creator, art),
        (ntriples, publisher, w3org)
      )
     
    assertTrue("graphs must be isomorphic",isIsomorphicWith(expected, parsedGraph))
  }

  @Test()
  def read_long_n3s_in_chunks(): Unit = {
    import scala.io._

    val card: File = new File(this.getClass.getResource("/card.nt").toURI)
    val in = new FileInputStream(card)
    val bytes = new Array[Byte](randomSz)

    val card_random: File = new File(this.getClass.getResource("/card.random.nt").toURI)
    val inR = new FileInputStream(card_random)
    val bytesR = new Array[Byte](randomSz)

    val jenaCard = JenaModelFactory.createDefaultModel()
    jenaCard.read(new FileInputStream(card),null,"N-TRIPLE")
    assertEquals("Pure Jena should have read 354 triples in"+card.getPath,354,jenaCard.size())

    var chunk = ParsedChunk(parser.ntriples,parser.P.annotator(U))
    var chunkR = ParsedChunk(parser.ntriples,parser.P.annotator(U))

    //scala.io.Reader seemed to have a problem.
    //here we test Asynchronous IO, feeding in pieces at a time
    var inOpen = true
    var inROpen = true
    while (inOpen || inROpen) {
      if (inOpen) {
        try {
          val length = in.read(bytes)
          if (length > -1) {
            chunk = chunk.parse(new String(bytes, 0, length, "ASCII"))
          } else inOpen = false
        } catch {
          case e: IOException => inOpen = false
        }
      }
      if (inROpen) {
        try {
          val length = inR.read(bytesR)
          if (length > -1) {
            chunkR = chunkR.parse(new String(bytesR, 0, length, "ASCII"))
          } else inROpen = false
        } catch {
          case e: IOException => inROpen = false
        }
      }
    }



    val result = chunk.parser.result(chunk.acc)
    val resultR = chunkR.parser.result(chunkR.acc)

    val res = result.user.queue.toList.map(_.asInstanceOf[Triple])
    val resR = resultR.user.queue.toList.map(_.asInstanceOf[Triple])


    println("the last triple found was in card.nt was "+res.last)
    println("the last triple found was in card.random.nt was "+resR.last)

    assertNotSame("the results of reading both cards should be different lists",res,resR)

    assertTrue("error parsing card.nt - failed at "+result.position+" status="+result.status,result.isSuccess)
    assertTrue("error parsing card.random.nt - failed at "+resultR.position+" with status "+resultR.status,resultR.isSuccess)


    val g = m.Graph(res)
    val gR = m.Graph(resR)

    assertEquals("There should be 354 triples in "+card.getPath,354,g.size)
    assertEquals("There should be 354 triples in "+card_random.getPath,354,gR.size)

    assertTrue("the two graphs must be isomorphic",isIsomorphicWith(g,gR))

  }

  import parser.P._
  case class ParsedChunk(val parser: Parser[Unit],val acc: Accumulator[Char, X, Listener]) {
    def parse(buf: Seq[Char]) = {
      if (!buf.isEmpty) {
        val (tripleParser, newAccu) = parser.feedChunked(buf, acc, buf.size)
        ParsedChunk(tripleParser, newAccu)
      } else {
        this
      }
    }
  }


}