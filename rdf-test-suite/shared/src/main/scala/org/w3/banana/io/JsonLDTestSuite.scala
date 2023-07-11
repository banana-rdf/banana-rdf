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
import scala.util.Try

abstract class JsonLDTestSuite[Rdf <: RDF, JsonLdOut](using
    ops: Ops[Rdf],
    reader: RDFReader[Rdf, Try, JsonLd],
    writer: RDFWriter[Rdf, Try, JsonLdOut]
) extends SerialisationTestSuite[Rdf, JsonLd, JsonLdOut]("JSON-LD-1.1", "jsonld11"):

   val referenceGraphSerialisedForSyntax = """
  [
  {
    "@id": "http://www.w3.org/2001/sw/RDFCore/ntriples/",
    "http://purl.org/dc/elements/1.1/creator": [
    {
      "@value": "Art Barstow"
    },
    {
      "@value": "Dave Beckett"
    }
    ],
    "http://purl.org/dc/elements/1.1/publisher": [
    {
      "@id": "http://www.w3.org/"
    }
    ]
  }
  ]
   """
