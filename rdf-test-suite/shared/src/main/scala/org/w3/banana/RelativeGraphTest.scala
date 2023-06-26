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

import org.w3.banana.TestConstants.foafPre

import org.w3.banana.RDF.*

/** Not all frameworks support relative graphs -- or at least they require different implementations */
class RelativeGraphTest[Rdf <: RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
   import ops.given

   test("relative graphs") {
     val rg0 = rGraph.empty
     assertEquals(rg0.size, 0)
     val rg1 = rGraph(rTriple(rURI("/#i"), rURI(foafPre("homePage")), rURI("/")))
     assertEquals(rg1.size, 1)

     // tests to check that opaque types work: we cannot just cast rX down to X
     val err1 = compileErrors(
       """val normalUri : URI[Rdf] = rURI("/#i")"""
     )
     assert(err1.contains("Required: org.w3.banana.RDF.URI[Rdf]"))

     val err2 = compileErrors(
       """val normalTriple : Triple[Rdf] =
			    rTriple(rURI("/#i"),URI(foaf("homePage")),rURI("/"))"""
     )
     assert(err2.contains("Required: org.w3.banana.RDF.Triple[Rdf]"))

     val err3 = compileErrors(
       """val normalGraph : Graph[Rdf] =
			    rGraph(rTriple(rURI("/#i"),URI(foaf("homePage")),rURI("/")))"""
     )
     assert(err3.contains("Required: org.w3.banana.RDF.Graph[Rdf]"))

   }
end RelativeGraphTest
