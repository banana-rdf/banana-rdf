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

import org.w3.banana.{RDF, Ops}
import scala.util.Try

abstract class RdfXMLTestSuite[Rdf <: RDF](using
    ops: Ops[Rdf],
    reader: RDFReader[Rdf, Try, RDFXML],
    writer: RDFWriter[Rdf, Try, RDFXML]
) extends SerialisationTestSuite[Rdf, RDFXML, RDFXML]("RDF/XML", "rdf"):

   val referenceGraphSerialisedForSyntax = """
  <rdf:RDF xmlns="http://purl.org/dc/elements/1.1/"
           xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <rdf:Description rdf:about="http://www.w3.org/2001/sw/RDFCore/ntriples/">
      <creator>Art Barstow</creator>
      <creator>Dave Beckett</creator>
      <publisher rdf:resource="http://www.w3.org/"/>
    </rdf:Description>
  </rdf:RDF>"""
