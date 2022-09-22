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

package org.w3.banana.io

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.w3.banana.*

import java.io.StringReader
import scala.util.Try
import _root_.io.lemonlabs.uri as ll

/** Not all formats allow serialisation of graphs with Relative URLs, so here we place tests
  * designed to stress test the implementations of those syntaxes.
  *
  * Relative RDF graphs are essential when POST-ing
  *
  * @param syntax
  *   e.g. "Turtle", ...
  * @param extension
  *   e.g. "ttl"
  * @param ops
  * @param reader
  * @param writer
  * @tparam Rdf
  * @tparam M
  *   is a Monad like Euther or Future, and not a Comonad. That just allows a uniform access to
  *   `get` and so is a terrible hack.
  * @tparam Sin
  *   Type for input Syntax
  * @tparam Sout
  *   Type of output Syntax
  */
abstract class RelativeGraphSerialisationTestSuite[Rdf <: RDF, Sin, Sout](
    syntax: String,
    extension: String
)(using
    ops: Ops[Rdf],
    reader: RDFReader[Rdf, Try, Sin],
    writer: RDFrWriter[Rdf, Try, Sout]
) extends AnyWordSpec with Matchers:

   import org.w3.banana.prefix.*

   import ops.{*, given}
   import RDF.*

   val wac  = WebACL[Rdf]
   val foaf = FOAF[Rdf]
   val rdf  = RDFPrefix[Rdf]
   val owl  = OWL[Rdf]

   val w3c: URI[Rdf] = URI("https://www.w3.org/")
   val timBlcardURI  = URI("https://www.w3.org/People/Berners-Lee/card")

   /** we use a realistic example from access control shown in the diagram given in
     * [[https://github.com/solid/authorization-panel/issues/210 issue 210]] of Solid Authorization
     * panel. `rootACL` is meant to be PUT to `</.acl>` of W3C Web server.
     */
   val admin  = rURI("/.acl#Admin")
   val aclPub = rURI("/.acl#Public")
   val rootACL = rGraph(
     rTriple(admin, rdf.`type`, wac.Authorization),
     rTriple(admin, wac.mode, wac.Control),
     rTriple(admin, wac.agent, rURI("/owner#i")),
     rTriple(admin, wac.default, rURI("/")),
     rTriple(aclPub, rdf.`type`, wac.Authorization),
     rTriple(aclPub, wac.default, rURI("/")),
     rTriple(aclPub, wac.mode, wac.Read),
     rTriple(aclPub, wac.agentClass, foaf.Agent)
   )

   /** This is the document that is to be POSTed with `Slug: card` onto the container
     * `</People/Berners-Lee/>` creating Tim's WebID.
     */
   val BLcard = rGraph(
     rTriple(rURI("#i"), rdf.`type`, foaf.Person),
     rTriple(rURI("#i"), foaf.name, Literal("Tim Berners-Lee")),
     rTriple(rURI("#i"), foaf.workInfoHomepage, rURI("/"))
   )

   val timbl = timBlcardURI.withFragment("i")
   val BLcardAbsolute = Graph(
     Triple(timbl, rdf.`type`, foaf.Person),
     Triple(timbl, foaf.name, Literal("Tim Berners-Lee")),
     Triple(timbl, foaf.workInfoHomepage, w3c)
   )

   /** This is the ACL that is meant to be PUT on `</People/Berners-Lee/.acl>`
     */
   val BLAcl = rGraph(
     rTriple(rURI("#TimRl"), rdf.`type`, wac.Authorization),
     rTriple(rURI("#TimRl"), wac.agent, rURI("card#i")),
     rTriple(rURI("#TimRl"), wac.mode, wac.Control),
     rTriple(rURI("#TimRl"), wac.default, rURI(".")),
     rTriple(rURI(""), owl.imports, rURI("/.acl"))
   )
   s"Writing the empty graph in $syntax" should {
     "not throw an exception" in {
       writer.asString(rGraph.empty).get
     }
   }

   s"Writing self references" should {
     val defaultACLGraph: rGraph[Rdf] = rGraph(rTriple(rURI(""), owl.imports, rURI(".acl")))
     "not throw an exception" in {
       writer.asString(defaultACLGraph).get
     }
   }

   s"writing relative graphs to $syntax and reading them back from correct base" should {

     "result in isomorphic graphs root container acl" in {
       // 1. we build a serialisation with relative URLs
       val rootACLStr: String = writer.asString(rootACL).get
       // 2. after PUTing it to the acl location, we fetch it and parse it with the relative URL location.
       val reconstructedGraph: RDF.Graph[Rdf] =
         reader.read(
           new StringReader(rootACLStr),
           ll.AbsoluteUrl.parse("https://www.w3.org/.acl")
         ).get
       val absoluteRootACLGr = rootACL.resolveAgainst(ll.AbsoluteUrl.parse(w3c.value))
       // 3. we compare the result with the absolutized graph we should have received
       assert(
         reconstructedGraph isomorphic absoluteRootACLGr,
         s"both graphs be isomorphic:\nresult=$reconstructedGraph\nshouldBe=$absoluteRootACLGr"
       )

     }

     "result in isomorphic graph for TimBL's card" in {
       // 1. we build a serialisation with relative URLs
       val BLCardStr: String = writer.asString(BLcard).get
       // 2. we POST it to Tim's Personal W3C Container with a Slug "card",
       //   then GET it the newly constructed resource and parse it with the new base,
       val reconstructedGraph =
         reader.read(new StringReader(BLCardStr), ll.AbsoluteUrl.parse(timBlcardURI.value)).get
       // 3. we compare the result with the absolutized graph we should have received
       assert(
         reconstructedGraph isomorphic BLcardAbsolute,
         s"both graphs be isomorphic:\nresult=$reconstructedGraph\nshouldBe=$BLcardAbsolute"
       )
     }

   }
