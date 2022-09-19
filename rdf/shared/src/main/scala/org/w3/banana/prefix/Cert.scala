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

object Cert:
   def apply[T <: RDF](using Ops[T]) = new Cert()

class Cert[T <: RDF](using ops: Ops[T])
    extends PrefixBuilder[T](
      "cert",
      ops.URI("http://www.w3.org/ns/auth/cert#")
    ):
   val key          = apply("key")
   val RSAKey       = apply("RSAKey")
   val RSAPublicKey = apply("RSAPublicKey")
   val exponent     = apply("exponent")
   val modulus      = apply("modulus")
end Cert
