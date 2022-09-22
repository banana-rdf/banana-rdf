/*
 *  Copyright (c) 2012 , 2021 W3C Members
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  This program and the accompanying materials are made available under
 *  the W3C Software Notice and Document License (2015-05-13) which is available at
 *  https://www.w3.org/Consortium/Legal/2015/copyright-software-and-document.
 *
 *  SPDX-License-Identifier: W3C-20150513
 */

package org.w3.banana
package io

import _root_.io.lemonlabs.uri as ll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.w3.banana.RDF.Literal

import java.io.*
import scala.util.Try

/** Test Serialisations. Some serialisations have one parser and multiple serialisers, such as with
  * json-ld, hence the distinction Sin and Sout
  */
abstract class SerialisationTestSuite[Rdf <: RDF, Sin, Sout](
    syntax: String,
    extension: String
)(using
    ops: Ops[Rdf],
    reader: RDFReader[Rdf, Try, Sin],
    writer: RDFWriter[Rdf, Try, Sout]
) extends AnyWordSpec with Matchers:

   /*
    * A simple serialisation for [SyntaxIn] of the [referenceGraph] below, with no relative urls
    *
    * To fill in for each Syntax.
    */
   def referenceGraphSerialisedForSyntax: String

   import RDF.*
   import ops.{*, given}

   def graphBuilder(prefix: Prefix[Rdf]): Graph[Rdf] =
      val ntriplesDoc = prefix("ntriples/")
      val creator     = URI("http://purl.org/dc/elements/1.1/creator")
      val publisher   = URI("http://purl.org/dc/elements/1.1/publisher")
      val dave        = Literal("Dave Beckett")
      val art         = Literal("Art Barstow")

      val w3org = URI("http://www.w3.org/")
      Graph(
        Triple(ntriplesDoc, creator, dave),
        Triple(ntriplesDoc, creator, art),
        Triple(ntriplesDoc, publisher, w3org)
      )

   val rdfCore: Option[String] = Some("http://www.w3.org/2001/sw/RDFCore/")
   val rdfCoreUrl: Option[ll.AbsoluteUrl] =
     rdfCore.flatMap(u => ll.AbsoluteUrl.parseTry(u).toOption)
   val rdfCorePrefix  = Prefix("rdf", rdfCore.get)
   val referenceGraph = graphBuilder(rdfCorePrefix)

   // TODO: there is a bug in Sesame with hash uris as prefix
   val foo       = "http://example.com/foo/"
   val fooPrefix = Prefix("foo", foo)
   val fooGraph  = graphBuilder(fooPrefix)

   s"simple $syntax string containing only absolute URIs" should {

     "parse using Readers (the base has no effect since all URIs are absolute)" in {
       val graph: RDF.Graph[Rdf] = reader.read(
         new StringReader(referenceGraphSerialisedForSyntax),
         ll.AbsoluteUrl.parse(rdfCore.get)
       ).get
       assert(referenceGraph isomorphic graph)
     }

     "parse using InputStream (the base has no effect since all URIs are absolute)" in {
       val graph: RDF.Graph[Rdf] = reader.read(
         new ByteArrayInputStream(referenceGraphSerialisedForSyntax.getBytes("UTF-8")),
         rdfCoreUrl.get
       ).get
       assert(referenceGraph isomorphic graph)
     }

   }

   s"write ref graph as $syntax string, read it & compare" in {
     val soutString =
       writer.asString(
         referenceGraph,
         ll.AbsoluteUrl.parseTry("http://www.w3.org/2001/sw/RDFCore/").toOption
       ).get
     assert(soutString.nonEmpty)
     val graph: RDF.Graph[Rdf] = reader.read(new StringReader(soutString), rdfCoreUrl.get).get
     assert(referenceGraph isomorphic graph)
   }

   "graphs with relative URIs" should {

     ", when moved to a new base, have all relative URLs transformed" in {
       val bar =
         for
            relativeSerialisation <- writer.asString(referenceGraph, rdfCoreUrl)
            computedFooGraph <-
              reader.read(new StringReader(relativeSerialisation), ll.AbsoluteUrl.parse(foo))
         yield
            println("rdfCore URL=" + rdfCoreUrl)
            println("relativeSer=" + relativeSerialisation)
            println("computedFooGr=" + computedFooGraph)
            println("fooGraph=" + fooGraph)
            computedFooGraph
       assert(fooGraph isomorphic bar.get)
     }

     """not be created just by taking URIs in absolute graphs and cutting the characters leading up to the base.
      It is more complex than that.
    """ in {
       import _root_.io.lemonlabs.uri.typesafe.dsl.*
       // asInstanceOf needed because of https://github.com/lemonlabsuk/scala-uri/issues/467
       val rdfCoreResource =
         rdfCoreUrl.map(u => (u.removeEmptyPathParts() / "imaginary").asInstanceOf[ll.AbsoluteUrl])
       val bar: Try[RDF.Graph[Rdf]] =
         for
            relativeSerialisation <- writer.asString(referenceGraph, rdfCoreResource)
            computedFooGraph <- reader.read(new StringReader(relativeSerialisation), rdfCoreUrl.get)
         yield computedFooGraph
       assert(referenceGraph isomorphic bar.get)
     }
   }
