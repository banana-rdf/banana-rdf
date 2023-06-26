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

   /** Most parsers relativize a graph with a base minimally. That is they only change URIs directly
     * tied to the base. But others relativize agressively, even using ../../.. type paths todo:
     * find a way to clearly distinguish these behaviors, as it can lead to very different test
     * resuls. By default the root w3 url is fixed.
     */
   def w3root(prefix: Prefix[Rdf]): RDF.URI[Rdf] =
      val root = ll.RelativeUrl(ll.UrlPath.slash, ll.QueryString.empty, None)
      prefix.prefixIri.baseFor(root)

   def graphBuilder(prefix: Prefix[Rdf]): Graph[Rdf] =
      val ntriplesDoc = prefix("ntriples/")
      val creator = URI("http://purl.org/dc/elements/1.1/creator")
      val publisher = URI("http://purl.org/dc/elements/1.1/publisher")
      val dave = Literal("Dave Beckett")
      val art = Literal("Art Barstow")
      Graph(
        Triple(ntriplesDoc, creator, dave),
        Triple(ntriplesDoc, creator, art),
        Triple(ntriplesDoc, publisher, w3root(prefix))
      )
   end graphBuilder

   val rdfCore: Option[String] = Some("http://www.w3.org/2001/sw/RDFCore/")
   val rdfCoreUrl: Option[ll.AbsoluteUrl] =
     rdfCore.flatMap(u => ll.AbsoluteUrl.parseTry(u).toOption)
   val rdfCorePrefix = Prefix("rdf", rdfCore.get)
   val referenceGraph = graphBuilder(rdfCorePrefix)

   // TODO: there is a bug in Sesame with hash uris as prefix
   // note: some parsers compact urls to ../../../ in which case the foo URL has
   // to be of the same depth as the new one written to. Hence foo/bar/baz
   val foo = "http://example.com/foo/bar/baz/"
   val fooPrefix = Prefix("foo", foo)

   s"simple $syntax string containing only absolute URIs" should {

     "parse using Readers (the base has no effect since all URIs are absolute)" in {
       val graph: RDF.Graph[Rdf] = reader.read(
         new StringReader(referenceGraphSerialisedForSyntax),
         ll.AbsoluteUrl.parse(rdfCore.get)
       ).get
       assert(referenceGraph isomorphic graph, (referenceGraph.triples, graph.triples))
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

     // changing the base does not give consistent results
     // see https://github.com/bblfish/banana-rdf/issues/5
     ", when fetched from the same base, we get back the original graph" in {
       val bar =
         for
            relativeSerialisation <- writer.asString(referenceGraph, rdfCoreUrl)
            computedFooGraph <-
              reader.read(new StringReader(relativeSerialisation), rdfCoreUrl.get)
         yield computedFooGraph
       assert(referenceGraph isomorphic bar.get, (referenceGraph.triples.toSeq, bar))
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
