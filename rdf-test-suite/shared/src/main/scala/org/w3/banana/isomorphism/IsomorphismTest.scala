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

package org.w3.banana.isomorphism

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.w3.banana.*
import org.w3.banana.prefix.*
// because AnyWordSpec is not yet declared open
import scala.language.adhocExtensions

/** Generic Tests for isomorphism functions that use whatever the Ops implementation chooses */
open class IsomorphismTest[Rdf <: RDF](using ops: Ops[Rdf])
    extends AnyWordSpec with Matchers with IsomorphismBNodeTrait[Rdf]:

   import ops.{given, *}

   "simple isomorphism tests" should {

     "a 1 triple ground graph" in {
       val g1 = Graph(Triple(hjs, foaf.name, Literal("Henry Story")))
       val expected = Graph(Triple(hjs, foaf.name, Literal("Henry Story")))
       (g1 isomorphic expected) `shouldEqual` true

       val nonExpected = Graph(Triple(hjs, foaf.name, Literal("Henri Story")))
       !(g1 isomorphic nonExpected) `shouldEqual` true
     }

     "two grounded graphs with 2 relations" in {
       val g1 = groundedGraph
       val expected = groundedGraph
       (g1 isomorphic expected) `shouldEqual` true
     }

     "list of size 1" in {
       val g = list(1, "h")
       val expected = list(1, "g")
       (g isomorphic expected) `shouldEqual` true
     }

     "list of size 2" in {
       val g = list(2, "h")
       val expected = list(2, "g")
       (g isomorphic expected) `shouldEqual` true
     }

     "list of size 5" in {
       val g = list(5, "h")
       val expected = list(5, "g")
       (g isomorphic expected) `shouldEqual` true
     }

   }
