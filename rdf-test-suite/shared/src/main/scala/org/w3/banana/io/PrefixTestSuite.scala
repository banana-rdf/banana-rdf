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

import org.w3.banana.{Ops, Prefix, RDF}

import scala.None
import scala.util.Try

// note all of these tests are a little fragile: serialisers could produce output just slightly differernt
// yet correct, e.g. with two spaces or tabs uses as seperators
abstract class PrefixTestSuite[Rdf <: RDF](using
    ops: Ops[Rdf],
    reader: RDFReader[Rdf, Try, Turtle],
    val writer: RDFWriter[Rdf, Try, Turtle]
) extends SerialisationTestSuite[Rdf, Turtle, Turtle]("Turtle", "ttl"):
   val referenceGraphSerialisedForSyntax =
     """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """

   "write with prefixes" in {
     val prefix = Set(Prefix[Rdf]("foo", "http://purl.org/dc/elements/1.1/"))

     //    val expectedString =
     //      """
     //        |@prefix foo:   <http://purl.org/dc/elements/1.1/> .
     //        |
     //        |<http://www.w3.org/2001/sw/RDFCore/ntriples/>
     //        |        foo:creator    "Dave Beckett" , "Art Barstow" ;
     //        |        foo:publisher  <http://www.w3.org/> .""".stripMargin

     val withPrefix = writer.asString(referenceGraph, None, prefix).get
     withPrefix `should` include("@prefix foo:")
     withPrefix `should` include("foo:creator")
     withPrefix `should` include("foo:publisher")
     withPrefix `should` not `include` ("<http://purl.org/dc/elements/1.1/creator>")
     withPrefix `should` not `include` ("<http://purl.org/dc/elements/1.1/publisher>")
   }

   "write without prefixes" in {
     val noPrefix = writer.asString(referenceGraph, None).get
     noPrefix `should` not `include` ("@prefix foo:")
     noPrefix `should` not `include` ("foo:creator")
     noPrefix `should` not `include` ("foo:publisher")
     noPrefix `should` include("<http://purl.org/dc/elements/1.1/creator>")
     noPrefix `should` include("<http://purl.org/dc/elements/1.1/publisher>")
   }

   "write with 3 prefixes" in {
     val prefixes = Set(
       Prefix[Rdf]("dc", "http://purl.org/dc/elements/1.1/"),
       Prefix[Rdf]("w3", "http://www.w3.org/"),
       Prefix[Rdf]("ntriples", "http://www.w3.org/2001/sw/RDFCore/ntriples/")
     )
     val withPrefix = writer.asString(referenceGraph, None, prefixes).get
     withPrefix `should` include("@prefix dc:")
     withPrefix `should` include("@prefix w3:")
     withPrefix `should` include("@prefix ntriples:")
     withPrefix `should` include("w3: .")
     withPrefix `should` include("ntriples: ")
     withPrefix `should` not `include` ("<http://purl.org/dc/elements/1.1/creator>")
     withPrefix `should` not `include` ("<http://purl.org/dc/elements/1.1/publisher>")
   }

end PrefixTestSuite
