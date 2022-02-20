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

package org.w3.banana.rdflib.test

import org.w3
import org.w3.banana
import org.w3.banana.rdflib
import org.w3.banana.rdflib.facade.storeMod.IndexedFormula

object Test:

   import org.w3.banana.RDF
   import org.w3.banana.rdflib.Rdflib
   import org.w3.banana.rdflib.Rdflib.{*, given}
   type R = Rdflib.type

   def main(args: Array[String]): Unit =
      val ops: org.w3.banana.Ops[R] = org.w3.banana.rdflib.Rdflib.ops
      import ops.{given, *}
      val bbl: RDF.URI[R]    = ops.URI("https://bblfish.net/#i")
      val fn: RDF.URI[R]     = ops.URI("https://xmlns.com/foaf/0.1/name")
      val hn: RDF.Literal[R] = ops.Literal("Henry")
      val t1: RDF.Triple[R]  = ops.Triple(bbl, fn, hn)
      val g                  = ops.Graph(t1)
      println("g=" + g)
      val ig: IndexedFormula = g.asInstanceOf[IndexedFormula]
      println("index=" + ig.index)

   //		println(s"is $nn an RDF Object?" + mod.isRDFlibObject(nn.asInstanceOf[js.Any]))
