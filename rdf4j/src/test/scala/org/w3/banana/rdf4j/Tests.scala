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

package org.w3.banana.rdf4j

import org.w3.banana.rdf4j.Rdf4j.{R, given}

class Rdf4jUriTest         extends org.w3.banana.URITest[R]
class Rdf4jGraphTest       extends org.w3.banana.GraphTest[R]
class Rdf4jGraphSearchTest extends org.w3.banana.GraphSearchTest[R]

class Rdf4jStoreTest extends org.w3.banana.StoreTest[R]

class Rdf4jPGTest extends org.w3.banana.PGTest[R]

class Rdf4PrefixTest extends org.w3.banana.PrefixTest[R]

class Rdf4TripleTest extends org.w3.banana.TripleTest[R]

class Rdf4jIsomorphismTest extends org.w3.banana.isomorphism.IsomorphismTest[R]

class Rdf4jDieselRelGraphConstructionTest
    extends org.w3.banana.diesel.DieselRelativeGraphConstructTest[R]
