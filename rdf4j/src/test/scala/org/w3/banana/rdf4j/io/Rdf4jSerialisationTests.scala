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

package org.w3.banana.rdf4j.io

import org.w3.banana.rdf4j.Rdf4j.R
import org.w3.banana.io.{AbsoluteRDFReader, NTriples}

import scala.util.Try

//todo: move this to the library
given gg: AbsoluteRDFReader[R, Try, NTriples] = org.w3.banana.io.NTriplesReader[R]

class Rdf4jNTripleReaderTests extends org.w3.banana.io.NTriplesReaderTests[R]