/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/


package org.w3.rdf.n3

import java.io._
import org.w3.rdf._
import nomo.Accumulator
import scala.util.Random
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.PropSpec

/**
 * Test parser with official tests from w3c using another turtle parser as a reference point
 *
 * @author bblfish
 * @created 27/02/2012
 */
abstract class TurtleParserTest[Rdf <: RDF, F, E, X, Rdf2 <: RDF](val ops: RDFOperations[Rdf],
                                                     val parser: TurtleParser[Rdf, F, E, X, Listener[Rdf]],
                                                     val otherTurtleReader: TurtleReader[Rdf2])
  extends PropSpec with PropertyChecks with ShouldMatchers {

  val morpheus: GraphIsomorphism[Rdf2]

  import ops._

  lazy val prime: Stream[Int] = 2 #:: Stream.from(3).filter(i =>
    prime.takeWhile(j => j * j <= i).forall(i % _ > 0))

  object rdfTransformer extends RDFTransformer[Rdf,Rdf2](ops,otherTurtleReader.ops)
  implicit def U = new Listener(ops)

  def randomSz = {
    prime(5+Random.nextInt(248))
  }


  property("the turtle parser should parse the NTriples test case") {
    val testFile =  new File(this.getClass.getResource("/www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt").toURI)
    info("testing file "+testFile.getName + " at "+testFile.getPath)

    val otherReading = otherTurtleReader.read(testFile,"")
    assert(otherReading.isRight === true,otherTurtleReader +" could not read the "+testFile+" returned "+otherReading)


    import otherTurtleReader.ops.graphAsIterable
    val otherAsSet = graphAsIterable(otherReading.right.get).toIterable.toSet
    info("read ntriples file with " +parser+" found "+otherAsSet.size +" triples")


    var chunk = ParsedChunk(parser.turtleDoc,parser.P.annotator(U))
    var inOpen = true
    val in = new FileInputStream(testFile)
    val bytes = new Array[Byte](randomSz)
    info("setting Turtle parser to reading with chunk size of "+bytes.size+" bytes")

    while (inOpen ) {
      try {
        val length = in.read(bytes)
        if (length > -1) {
          chunk = chunk.parse(new String(bytes, 0, length, "ASCII"))
        } else inOpen = false
      } catch {
        case e: IOException => inOpen = false
      }
    }

    val result = chunk.parser.result(chunk.acc)

    val res = result.user.queue.toList.map(_.asInstanceOf[Triple])
    if (res.size!=30) {
      info("the last triple read was "+res.last)
      assert(res.size === 30,"the Turtle parser did not parse the right number of triples")
    }
    val g = Graph(res)

    val gAsOther = rdfTransformer.transform(g)

    val isomorphic = morpheus.isomorphism(gAsOther, otherReading.right.get)

    if (!isomorphic) {
      info("graphs were not isomorphic - trying to narrow down on problem statement")

      //this does not work very well yet. We need a contains method on a graph that works for each implementation
      res.foreach {
        var counter = 0
        triple =>
          counter += 1


          val res = otherAsSet.contains( rdfTransformer.transformTriple(triple) )

          assert(res,"the other graph does not contain triple no "+ counter +": "+triple)
      }

      assert(false,"graphs were not isomorphic")
    }

  }

  import parser.P._
  case class ParsedChunk( val parser: Parser[Unit],
                          val acc: Accumulator[Char, X, Listener[Rdf]] ) {
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