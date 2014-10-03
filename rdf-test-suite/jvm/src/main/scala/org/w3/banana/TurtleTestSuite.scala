package org.w3.banana

import java.io._

import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

abstract class TurtleTestSuite[Rdf <: RDF]()(implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, Turtle], writer: RDFWriter[Rdf, Turtle])
    extends WordSpec with Matchers {

  import ops._

  def graphBuilder(prefix: Prefix[Rdf]) = {
    val ntriplesDoc = prefix("ntriples/")
    val creator = URI("http://purl.org/dc/elements/1.1/creator")
    val publisher = URI("http://purl.org/dc/elements/1.1/publisher")
    val dave = Literal("Dave Beckett")
    val art = Literal("Art Barstow")
    val w3org = URI("http://www.w3.org/")
    Graph(
      Triple(ntriplesDoc, creator, dave),
      Triple(ntriplesDoc, creator, art),
      Triple(ntriplesDoc, publisher, w3org)
    )
  }

  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val rdfCorePrefix = Prefix("rdf", rdfCore)
  val referenceGraph = graphBuilder(rdfCorePrefix)

  // TODO: there is a bug in Sesame with hash uris as prefix
  val foo = "http://example.com/foo/"
  val fooPrefix = Prefix("foo", foo)
  val fooGraph = graphBuilder(fooPrefix)

  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/jvm/src/main/resources/card.ttl")
    val fis = new FileInputStream(file)
    try {
      val graph = reader.read(fis, file.toURI.toString).get
      graph.size should equal(77)
    } finally { fis.close() }
  }

  "read simple TURTLE String" in {
    val turtleString = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
 """
    val graph = Await.result(reader.read(turtleString, rdfCore), Duration(1, SECONDS))
    assert(referenceGraph isIsomorphicWith graph)

  }

  "write simple graph as TURTLE string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").get
    turtleString should not be ('empty)
    val graph = Await.result(reader.read(turtleString, rdfCore), Duration(1, SECONDS))
    assert(referenceGraph isIsomorphicWith graph)
  }

  "works with relative uris" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    val bar = for {
      turtleString <- Future.successful(writer.asString(referenceGraph, rdfCore))
      computedFooGraph <- reader.read(turtleString.get, foo)
    } yield {
      computedFooGraph
    }
    val g: Rdf#Graph = Await.result(bar, Duration(1, SECONDS))
    assert(fooGraph isIsomorphicWith g)
  }

}
