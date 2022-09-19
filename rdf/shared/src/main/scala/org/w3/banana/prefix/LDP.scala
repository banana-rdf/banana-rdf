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

object LDP:
   def apply[T <: RDF](using Ops[T]) = new LDP[T]

class LDP[R <: RDF](using ops: Ops[R])
    extends PrefixBuilder[R](
      "ldp",
      ops.URI("http://www.w3.org/ns/ldp#")
    ):
   val AggregateContainer      = apply("AggregateContainer")
   val CompositeContainer      = apply("CompositeContainer")
   val Container               = apply("Container")
   val Page                    = apply("Page")
   val Resource                = apply("Resource")
   val containerSortPredicates = apply("containerSortPredicates")
   val membershipPredicate     = apply("membershipPredicate")
   val membershipSubject       = apply("membershipSubject")
   val nextPage                = apply("nextPage")
   val created                 = apply("created")
   val pageOf                  = apply("pageOf")
end LDP
