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

package org.w3.banana.jena

import org.w3.banana.jena.JenaRdf.{R, given}

class JenaUriTest extends org.w3.banana.URITest[R]

class JenaGraphTest extends org.w3.banana.GraphTest[R]
class JenaGraphSearchTest extends org.w3.banana.GraphSearchTest[R]

class JenaStoreTest extends org.w3.banana.StoreTest[R]

class JenaPGTest extends org.w3.banana.PGTest[R]

class JenaPrefixTest extends org.w3.banana.PrefixTest[R]

class JenaTripleTest extends org.w3.banana.TripleTest[R]

class JenaIsomorphismTest extends org.w3.banana.isomorphism.IsomorphismTest[R]

class JenaDiesaelRelGraphConstructionTest
    extends org.w3.banana.diesel.DieselRelativeGraphConstructTest[R]
