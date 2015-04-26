package org.w3.banana
package io

import java.io._

import scalaz._
import scalaz.syntax._
import comonad._
import monad._
import org.scalatest.{Matchers, WordSpec}

/**
 * Test Serialisations. Some serialisations have one parser and multiple serialisers, such
 * as with json-ld, hence the distinction Sin and Sout
 */
abstract class SerialisationTestSuite[Rdf <: RDF, M[+_] : Monad : Comonad, Sin, Sout](
  syntax: String,
  extension: String
)(implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, Sin],
  writer: RDFWriter[Rdf, M, Sout]
) extends WordSpec with Matchers {

  // both Monad and Comonad are Functors, so they compete for the
  // syntax. So we choose arbitrarily one of them.
  // TODO @betehess to ask scalaz people
  val M = Monad[M]
  import M.functorSyntax._

  /*
   * A simple serialisation for [SyntaxIn] of the [referenceGraph] below, with no relative urls
   * 
   * To fill in for each Syntax.
   */
  def referenceGraphSerialisedForSyntax: String

  import ops._

  def graphBuilder(prefix: Prefix[Rdf]): Rdf#Graph = {
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

  s"simple $syntax string containing only absolute URIs" should {

    "parse using Readers (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(new StringReader(referenceGraphSerialisedForSyntax), rdfCore).copoint
      assert(referenceGraph isIsomorphicWith graph)
    }

    "parse using InputStream (the base has no effect since all URIs are absolute)" in {
      val graph = reader.read(
        new ByteArrayInputStream(referenceGraphSerialisedForSyntax.getBytes("UTF-8")), rdfCore
      ).copoint
      assert(referenceGraph isIsomorphicWith graph)
    }

  }

  s"write ref graph as $syntax string, read it & compare" in {
    val soutString =
      writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").copoint
    assert(soutString.nonEmpty)
    val graph = reader.read(new StringReader(soutString), rdfCore).copoint
    assert(referenceGraph isIsomorphicWith graph)
  }

  "graphs with relative URIs" should {

    ", when moved to a new base, have all relative URLs transformed" in {
      val bar = for {
        relativeSerialisation <- writer.asString(referenceGraph, rdfCore)
        computedFooGraph <- reader.read(new StringReader(relativeSerialisation), foo)
      } yield {
        computedFooGraph
      }

      assert(fooGraph isIsomorphicWith bar.copoint)
    }

    """not be created just by taking URIs in absolute graphs and cutting the characters leading up to the base.
      It is more complex than that.
    """ in {
      val rdfCoreResource = rdfCore + "imaginary"
      val bar = for {
        relativeSerialisation <- writer.asString(referenceGraph, rdfCoreResource)
        computedFooGraph <- reader.read(new StringReader(relativeSerialisation), rdfCore)
      } yield {
        computedFooGraph
      }
      assert(referenceGraph isIsomorphicWith bar.copoint)
    }
  }

}
