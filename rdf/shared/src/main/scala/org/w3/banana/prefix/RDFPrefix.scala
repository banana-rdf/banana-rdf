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

package org.w3.banana.prefix

import org.w3.banana.{RDF, Ops, PrefixBuilder}

object RDFPrefix:
   def apply[Rdf <: RDF](using Ops[Rdf]) = new RDFPrefix()

class RDFPrefix[Rdf <: RDF](using ops: Ops[Rdf])
    extends PrefixBuilder[Rdf](
      "rdf",
      ops.URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#")
    ):
   val langString = apply("langString") // todo: does not exist in ontology
   val nil = apply("nil")
   val typ = apply("type")
   val Alt = apply("Alt")
   val Bag = apply("Bag")
   val List = apply("List")
   val PlainLiteral = apply("PlainLiteral")
   val Property = apply("Property")
   val Seq = apply("Seq")
   val Statement = apply("Statement")
   val XMLLiteral = apply("XMLLiteral")
   val first = apply("first")
   val langRange = apply("langRange")
   val obj = apply("object")
   val predicate = apply("predicate")
   val rest = apply("rest")
   val subject = apply("subject")
   val `type` = apply("type")
   val value = apply("value")
end RDFPrefix
