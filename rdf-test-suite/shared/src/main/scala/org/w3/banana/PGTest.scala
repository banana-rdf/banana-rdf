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

import org.w3.banana.RDF
import RDF.*

open class PGTest[Rdf <: RDF](using ops: Ops[Rdf]) extends munit.FunSuite:
   import ops.{given, *}
   import TestConstants.*

   test("PG Creation") {
     assertEquals(Graph.empty.size, 0)
     val timbl: URI[Rdf]   = URI(tim("i"))
     val timEmpty: PG[Rdf] = PG(timbl)
     // note: in order for the implicit conversion to take hold we need to specify the upper bound
     assertEquals[RDF.Node[Rdf], RDF.Node[Rdf]](timEmpty.pointer, timbl)
     // no: graphs should be compared with isomorphism
     // assertEquals(timEmpty.graph,Graph.empty)
   }
end PGTest
