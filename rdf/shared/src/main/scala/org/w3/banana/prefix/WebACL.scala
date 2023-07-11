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

import org.w3.banana.{Ops, PrefixBuilder, RDF}

object WebACL:
   def apply[R <: RDF](using Ops[R]) = new WebACL[R]()

class WebACL[R <: RDF](using ops: Ops[R])
    extends PrefixBuilder[R](
      "acl",
      ops.URI("http://www.w3.org/ns/auth/acl#")
    ):
   val Authorization = apply("Authorization")
   val agent = apply("agent")
   val agentClass = apply("agentClass")
   val accessTo = apply("accessTo")
   val accessToClass = apply("accessToClass")
   val default = apply("default")
   val mode = apply("mode")
   val Access = apply("Access")
   val Read = apply("Read")
   val Write = apply("Write")
   val Append = apply("Append")
   val accessControl = apply("accessControl")
   val Control = apply("Control")
   val owner = apply("owner")
   val WebIDAgent = apply("WebIDAgent")

   // not officially supported:
   val include = apply("import")
end WebACL
