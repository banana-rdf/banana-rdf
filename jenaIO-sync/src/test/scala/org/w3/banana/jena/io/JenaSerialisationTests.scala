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

package org.w3.banana.jena.io

import org.w3.banana.jena.JenaRdf.R
import org.w3.banana.io.{AbsoluteRDFReader, JsonLdCompacted, NTriples}

import scala.util.Try

//todo: move this to the library
given gg: AbsoluteRDFReader[R, Try, NTriples] = org.w3.banana.io.NTriplesReader[R]

class JenaNTripleReaderTests extends org.w3.banana.io.NTriplesReaderTests[R]

//todo: group readers and writers together
import org.w3.banana.jena.io.JenaRDFWriter.given
import org.w3.banana.jena.io.JenaRDFReader.given

class JenaTurtleTestSuite extends org.w3.banana.io.TurtleTestSuite[R]

class JenaRelativeTurtleTestSuite extends org.w3.banana.io.RelativeTurtleTestSuite[R]

//class JenaRelativeJsonLDTestSuite extends org.w3.banana.io.RelativeJsonLDTestSuite[R]


class JenaRdfXMLTestSuite extends org.w3.banana.io.RdfXMLTestSuite[R]

class JenaJsonLDTestSuite extends org.w3.banana.io.JsonLDTestSuite[R, JsonLdCompacted]

class JenaPrefixTestSuite extends org.w3.banana.io.PrefixTestSuite[R]

//todo prefix test suites for all the other formats that accept prefixes! (eg jsonld)