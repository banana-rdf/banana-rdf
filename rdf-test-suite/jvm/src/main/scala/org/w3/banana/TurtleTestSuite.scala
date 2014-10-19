package org.w3.banana

import java.io._

import org.scalatest._
import org.w3.banana.io._

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

  "simple TURTLE string containing only absolute URIs" should {
    val turtleString = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
 """

    "parse using Readers (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(new StringReader(turtleString), rdfCore).get
      assert(referenceGraph isIsomorphicWith graph)
    }

    "parse using InputStream (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(new ByteArrayInputStream(turtleString.getBytes("UTF-8")), rdfCore).get
      assert(referenceGraph isIsomorphicWith graph)
    }

  }

  "write simple graph as TURTLE string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").get
    turtleString should not be ('empty)
    val graph = reader.read(new StringReader(turtleString), rdfCore).get
    assert(referenceGraph isIsomorphicWith graph)
  }

  "graphs with relative URIs" should {

    "Moving the graph to a new base should transform all relative URLs" in {
      val bar = for {
        turtleStringWithRelativeURIs <- writer.asString(referenceGraph, rdfCore)
        computedFooGraph <- reader.read(new StringReader(turtleStringWithRelativeURIs), foo)
      } yield {
        computedFooGraph
      }
      assert(fooGraph isIsomorphicWith bar.get)
    }

    "relativising a graph to a base requires URI.relativise function" in {
      val rdfCoreSubstring = rdfCore.substring(0, rdfCore.length - 3)
      val bar = for {
        turtleStringWithRelativeURIs <- writer.asString(referenceGraph, rdfCoreSubstring)
        computedFooGraph <- reader.read(new StringReader(turtleStringWithRelativeURIs), "http://neverused.example/")
      } yield {
        computedFooGraph
      }
      println(s"fooGraph=$fooGraph")
      println(s"bar=$bar")
      assert(referenceGraph isIsomorphicWith bar.get)

    }
  }

}
