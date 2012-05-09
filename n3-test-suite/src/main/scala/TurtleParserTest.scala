/*
* Copyright (c) 2012 Henry Story
* under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
*/


package org.w3.banana.n3

import java.io._
import org.w3.banana._
import _root_.nomo.Accumulator //fix for IntelliJ 11 and 11.1beta, because we have a nomo package
import scala.util.Random
import org.scalatest.matchers.ShouldMatchers
import java.net.URI
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FailureOf, PropSpec}

import scalaz._
import scalaz.Validation._

/**
 * Test parser with official tests from w3c using another turtle parser as a reference point
 *
 * todo: with a little bit of work, this could be generalised to test any parser.
 *
 * @param testedParser the turtle parser to be tested
 * @param referenceParser only needs to understand NTriples. It reads the expected results
 * @tparam Rdf The RDF framework understood by the test parser
 * @tparam Rdf2 The RDF framework with which the reference parser uses
 */
abstract class TurtleParserTest[Rdf <: RDF, Rdf2 <: RDF](
    val testedParser: RDFReader[Rdf, Turtle],
    val referenceParser: RDFReader[Rdf2, Turtle],
    val ops: RDFOperations[Rdf],
    val ops2: RDFOperations[Rdf2])
extends PropSpec with PropertyChecks with ShouldMatchers with FailureOf {

  val morpheus: GraphIsomorphism[Rdf2]

  import ops._

  lazy val prime: Stream[Int] = 2 #:: Stream.from(3).filter(i =>
    prime.takeWhile(j => j * j <= i).forall(i % _ > 0))

  object rdfTransformer extends RDFTransformer[Rdf,Rdf2](ops, ops2)

  def randomSz = {
    prime(5+Random.nextInt(248))
  }

  val tstDir = new File(this.getClass.getResource("/www.w3.org/TR/turtle/tests/").toURI)
  val ttlFiles = tstDir.listFiles().filter(_.getName.endsWith(".ttl"))
  val bad= ttlFiles.filter(_.getName.startsWith("bad"))
  val good = ttlFiles.diff(bad).filter(!_.getName.contains("manifest"))

  //todo: the readers should return the triples as Iterables so that one can know where things went wrong.
  def isomorphicTest(result: Rdf#Graph, referenceResult: Rdf2#Graph) {
    val gAsOther = rdfTransformer.transform(result)

    val isomorphic = morpheus.isomorphism(gAsOther, referenceResult)

    if (!isomorphic) {
      info("graphs were not isomorphic - trying to narrow down on problematic statement")

      import ops2.graphAsIterable
      val referenceAsSet = graphAsIterable(referenceResult).toIterable.toSet
      val resultAsSet = graphAsIterable(gAsOther).toIterable.toSet
      info("read ntriples file with " +testedParser+" found "+referenceAsSet.size +" triples")

      if (resultAsSet.size!=referenceAsSet.size) {
        info("    The number of triples read was "+resultAsSet.size+". The reference number was "+referenceAsSet.size)
        //todo: specify the last returned Result from the tested graph
      }

      //test each statement
      //this does not work very well yet. We need a contains method on a graph that works for each implementation
      resultAsSet.foreach {
        var counter = 0
        triple =>
          counter += 1
          val res = referenceAsSet.contains(triple)
          if (!res) info("     missing triple no " + counter + ": " + triple+" from reference graph.")
      }

      assert(false, "     graphs were not isomorphic")
    }
  }

  property("the Turtle parser should parse the official NTriples test case") {
    val testFile =  new File(this.getClass.getResource("/www.w3.org/2000/10/rdf-tests/rdfcore/ntriples/test.nt").toURI)
    info("testing file "+testFile.getName + " at "+testFile.getPath)

    val otherReading = referenceParser.read(testFile,"","UTF-8").fold(
      t => sys.error(referenceParser +" could not read the "+testFile),
      g => g
    )

    val result = testedParser.read(testFile,"","UTF-8").fold(t => throw t, g => g)

    isomorphicTest(result, otherReading)
  }

  property("The Turtle parser should pass each of the positive official W3C Turtle Tests") {
    val base = "http://www.w3.org/2001/sw/DataAccess/df1/tests/"
    info("all these files are in "+tstDir)

    val res = for(f <- good) yield {
      val resFileName = f.getName.substring(0,f.getName.length()-"ttl".length())+"out"
      val resultFile = new File(f.getParentFile,resFileName)
      info(" ")
      info("input "+f.getName+" should produce "+resultFile.getName)
      // TODO: don't use the conversion to either but embrace the Validation
      val otherReading = referenceParser.read(resultFile,base+f.getName).either
      val fail = failureOf {
        if (otherReading.isLeft){
          info("reference parser "+referenceParser + " could not read " + resultFile + " detail: " + otherReading.left)
          throw otherReading.left.get
       }
        info("base set to="+base+f.getName)
        val result = testedParser.read(f, base+f.getName,"UTF-8").either
        assert(result.isRight,"failed to parse test. Result was:"+result)
        isomorphicTest(result.right.get, otherReading.right.get)
      }
      if (fail!=None) {
        info("test for "+f.getName+" failed because "+fail.get.getMessage)
        val stack =new StringWriter()
        fail.get.printStackTrace(new PrintWriter( stack))
        info("stack trace:"+stack.getBuffer)
      }
      fail
    }
    val errs = res.filter(_!= None)

    assert(errs.size==0,errs.size +" of the tests failed out of a total of "+good.size)
  }

  property("The Turtle parser should fail each of the negative official W3C Turtle Tests") {
    val base = "http://www.w3.org/2001/sw/DataAccess/df1/tests/"
    info("all these files are in "+tstDir)

    val res = for(f <- bad) yield {
      val reading = testedParser.read(f,base+f.getName,"UTF-8").either
      if (reading.isRight) {
        info(" oops! results found"+reading)
      }
      reading
    }
    val errs = res.filter(_.isRight)
    assert(errs.size==0,errs.size +" of the tests wrongly succeeded out of a total of "+bad.size)
  }


}