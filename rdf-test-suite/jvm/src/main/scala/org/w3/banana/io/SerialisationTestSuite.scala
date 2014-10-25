package org.w3.banana.io

import java.io._

import org.scalatest._
import org.w3.banana.{ Prefix, RDF, RDFOps }

abstract class SerialisationTestSuite[Rdf <: RDF, S](implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, S], writer: RDFWriter[Rdf, S], syntax:  Syntax[S])
    extends WordSpec with Matchers {

  /* A simple serialisation for Syntax of the referenceGraph below.
   * 
   * To fill in for each Syntax.
   */
  def referenceGraphSerialisedForSyntax: String

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

  s"read ${syntax.defaultMimeType} version of timbl's card" in {
    WellKnownMimeExtensions.extension(syntax.mimeTypes.head).map { ext =>
      val file = new File(s"rdf-test-suite/jvm/src/main/resources/card.$ext")
      val fis = new FileInputStream(file)
      try {
        val graph = reader.read(fis, file.toURI.toString).get
        graph.size should equal(77)
      } finally {
        fis.close()
      }
    }
  }

  s"simple ${syntax.defaultMimeType} string containing only absolute URIs" should {

    "parse using Readers (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(new StringReader(referenceGraphSerialisedForSyntax), rdfCore).get
      assert(referenceGraph isIsomorphicWith graph)
    }

    "parse using InputStream (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(new ByteArrayInputStream(referenceGraphSerialisedForSyntax.getBytes("UTF-8")), rdfCore).get
      assert(referenceGraph isIsomorphicWith graph)
    }

  }

  s"write simple graph as ${syntax.defaultMimeType} string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").get
    turtleString should not be ('empty)
    val graph = reader.read(new StringReader(turtleString), rdfCore).get
    assert(referenceGraph isIsomorphicWith graph)
  }

  "graphs with relative URIs" should {

    ", when moved to a new base, have all relative URLs transformed" in {
      // println("referenceGraph=" + referenceGraph)
      val bar = for {
        relativeSerialisation <- writer.asString(referenceGraph, rdfCore)
        computedFooGraph <- reader.read(new StringReader(relativeSerialisation), foo)
      } yield {
        //        println(s"withRelURIs=$relativeSerialisation")
        computedFooGraph
      }
      //      println(s"fooGraph=$fooGraph")
      //      println(s"bar=$bar")
      assert(fooGraph isIsomorphicWith bar.get)
    }

    """not be created just by taking URIs in absolute graphs and cutting the characters leading up to the base.
      It is more complext than that.
    """ in {
      val rdfCoreResource = rdfCore + "imaginary"
      val bar = for {
        relativeSerialisation <- writer.asString(referenceGraph, rdfCoreResource)
        computedFooGraph <- reader.read(new StringReader(relativeSerialisation), rdfCore)
      } yield {
        // println(s"withRelURIs=$relativeSerialisation")
        computedFooGraph
      }
      // println(s"referenceGraph=$referenceGraph")
      // println(s"bar=$bar")
      assert(referenceGraph isIsomorphicWith bar.get)
    }
  }

}
