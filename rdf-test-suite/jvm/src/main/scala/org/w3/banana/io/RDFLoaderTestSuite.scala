package org.w3.banana.io

import org.w3.banana.RDFLoaderModule
import org.w3.banana.RDFOpsModule
import org.w3.banana.TurtleWriterModule
import java.io.File

import scala.util._
import org.scalatest._

trait RDFLoaderTestSuite extends WordSpec with RDFLoaderModule with RDFOpsModule
    with TurtleWriterModule with Matchers {
  import ops._

  val ttlDocURI = new File("rdf-test-suite/jvm/src/main/resources/card.ttl").toURI().toURL()
  val rdfxmlDocURI = new File("rdf-test-suite/jvm/src/main/resources/card.rdf").toURI().toURL()

  // One can also test an HTTP URL, but this is not suitable for a test suite:
  // val rdfxmlDocURI = new java.net.URL("https://www.w3.org/People/Berners-Lee/card#i" ) // works also !

  "rdfLoader " should {

    "load a RDF/XML URL (actually file here)" in {
      assert(toString(testRDFXML).contains("Tim Berners-Lee"))
    }

    "load a Turtle URL (actually file here)" in {
      assert(toString(testTTL).contains("Weaving the Web:"))
    }
  }

  def testRDFXML(): Rdf#Graph = {
    val graphUri = makeUri("urn:foaf")
    val graph = rdfLoader.load(rdfxmlDocURI)
    graph.get
  }
  def testTTL(): Rdf#Graph = {
    rdfLoader.load(ttlDocURI).get
  }

  def toString(graph: Rdf#Graph): String = {
    turtleWriter.asString(graph, "").get
  }
}
