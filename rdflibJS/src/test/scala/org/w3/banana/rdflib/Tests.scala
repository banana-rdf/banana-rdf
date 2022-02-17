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

package org.w3.banana.rdflib

import org.w3.banana.rdflib.Rdflib.{R, given}

class RdflibGraphTest extends org.w3.banana.GraphTest[R]

class RdflibGraphSearchTest extends org.w3.banana.GraphSearchTest[R]

class RdflibStoreTest extends org.w3.banana.StoreTest[R]

class RdflibPGTest extends org.w3.banana.PGTest[R]

class RdflibPrefixTest extends org.w3.banana.PrefixTest[R]

class RdflibTripleTest extends org.w3.banana.TripleTest[R]

class RdflibIsomorphismTest extends org.w3.banana.isomorphism.IsomorphismTest[R]
