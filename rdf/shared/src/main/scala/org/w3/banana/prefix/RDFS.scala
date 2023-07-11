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

object RDFS:
   def apply[T <: RDF](using Ops[T]) = new RDFS()

class RDFS[Rdf <: RDF](using ops: Ops[Rdf])
    extends PrefixBuilder[Rdf](
      "rdfs",
      ops.URI("http://www.w3.org/2000/01/rdf-schema#")
    ):
   val Class = apply("Class")
   val Container = apply("Container")
   val ContainerMembershipProperty = apply("ContainerMembershipProperty")
   val Datatype = apply("Datatype")
   val Literal = apply("Literal")
   val Resource = apply("Resource")
   val comment = apply("comment")
   val domain = apply("domain")
   val isDefinedBy = apply("isDefinedBy")
   val label = apply("label")
   val member = apply("member")
   val range = apply("range")
   val seeAlso = apply("seeAlso")
   val subClassOf = apply("subClassOf")
   val subPropertyOf = apply("subPropertyOf")
end RDFS
