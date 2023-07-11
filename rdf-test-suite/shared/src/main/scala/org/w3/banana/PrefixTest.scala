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

import org.w3.banana.prefix.{FOAF, RDFPrefix, XSD}

open class PrefixTest[Rdf <: RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
   import ops.given
   import TestConstants.*
   import org.w3.banana.operations.URI.*
   val foaf = FOAF[Rdf]
   val xsd = XSD[Rdf]
   val rdf = RDFPrefix[Rdf]

   test("FOAF Prefix") {
     assert(foaf.age == URI(foafPre("age")))
     assert(!(foaf.age != URI(foafPre("age"))))
     assertEquals(foaf.knows, URI(foafPre("knows")))
     assertEquals(foaf.homepage, URI(foafPre("homepage")))
   }

   test("XSD Prefix") {
     assertEquals(xsd.int, URI(xsdPre("int")))
     assertEquals(xsd.integer, URI(xsdPre("integer")))
     assertEquals(xsd.hexBinary, URI(xsdPre("hexBinary")))
     assertEquals(xsd.string, URI(xsdStr))
   }

   test("RDF namespace") {
     assertEquals(rdf.langString, URI(xsdLangStr))
   }

end PrefixTest
