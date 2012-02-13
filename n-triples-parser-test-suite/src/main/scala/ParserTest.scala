package org.w3.rdf

import org.junit.Test
import org.junit.Assert._
import util.Random
import java.io._
import nomo.{Success, Accumulator}

// would be happy to use
// NTriplesParserTest[M <: Model](m: M, parser: NTriplesParser[m.type], isomorphism: GraphIsomorphism[m.type])
// but the compiler complains, saying it does not know m
abstract class NTriplesParserTest[M <: Module, F, E, X](val parser: NTriplesParser[M, F, E, X, Unit]) {

  implicit val U: Unit = ()
  val isomorphism: GraphIsomorphism[parser.m.type]
  
  import parser.m._
  import isomorphism._

  /** so that this test can be run with different IO models */
  def toF(string: String): F

  val n3 ="""
  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett" .
  <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Art Barstow" .

 <http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .

 """
  
  @Test()
  def read_simple_n3(): Unit = {

    val res = parser.ntriples(n3).get
    val parsedGraph = parser.m.Graph(res)
    assertEquals("should be three triples in graph",3,parsedGraph.size)

    val ntriples = IRI("http://www.w3.org/2001/sw/RDFCore/ntriples/")
    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
    val dave = TypedLiteral("Dave Beckett", xsdStringIRI)
    val art = TypedLiteral("Art Barstow")
    val w3org = IRI("http://www.w3.org/")
    
    val expected = 
      Graph(
        Triple(ntriples, creator, dave),
        Triple(ntriples, creator, art),
        Triple(ntriples, publisher, w3org)
      )
//    assertEquals("The two graphs must have the same size",expected.size,parsedGraph.size)

    assertTrue("graphs must be isomorphic",isIsomorphicWith(expected, parsedGraph))
  }

  @Test()
  def read_long_n3s_in_chunks(): Unit = {
    import scala.io._

    def randomSz = {
      val random = 29 to 47+1
      random(Random.nextInt(random.length ))
    }
    val card: File = new File(this.getClass.getResource("/card.nt").toURI)
    System.out.println("reading in "+card.getAbsolutePath)
    val in = Source.fromFile(card,"ASCII").grouped(randomSz)

    val card_random: File = new File(this.getClass.getResource("/card.random.nt").toURI)
    System.out.println("reading in "+card_random.getAbsolutePath)
    val inR = Source.fromFile(card_random,"ASCII").grouped(randomSz)

   import parser.P._
    case class ParsedChunk(val parser: Parser[List[Triple]],val acc: Accumulator[Char, X, Unit]) {
      def parse(buf: Seq[Char]) = {
        if (!buf.isEmpty) {
          val (tripleParser, newAccu) = parser.feedChunked(buf, acc, buf.size)
          ParsedChunk(tripleParser, newAccu)
        } else {
          this
        }
      }
    }

    var chunk = ParsedChunk(parser.ntriples,parser.P.annotator())
    var chunkR = ParsedChunk(parser.ntriples,parser.P.annotator())

    while (in.hasNext && inR.hasNext) {
      if (in.hasNext) {
        chunk = chunk.parse(in.next())
      }
      if (inR.hasNext) {
        chunkR = chunk.parse(inR.next())
      }
    }

    val res = chunk.parser.result(chunk.acc)
    val resR = chunkR.parser.result(chunkR.acc)

    assertTrue("error parsing card.nt - failed at "+res.position+" status="+res.status,res.isSuccess)
    assertTrue("error parsing card.random.nt - failed at "+res.position+" with status "+res.status,resR.isSuccess)

    val g = parser.m.Graph(res.get)
    val gR = parser.m.Graph(resR.get)

//    println("<<< "+diff(g, gR).size)
//    println(">>> "+diff(gR, g).size)
    
    assertEquals("The two graphs must have the same size",g.size,gR.size)

    assertTrue("the two graphs must be isomorphic",isIsomorphicWith(g,gR))

  }


}