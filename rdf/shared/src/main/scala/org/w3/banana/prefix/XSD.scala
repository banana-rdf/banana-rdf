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

object XSD:
   def apply[Rdf <: RDF](using Ops[Rdf]) = new XSD[Rdf]

class XSD[Rdf <: RDF](using val ops: Ops[Rdf])
    extends PrefixBuilder[Rdf](
      "xsd",
      "http://www.w3.org/2001/XMLSchema#"
    ):
   // http://www.w3.org/TR/owl-rdf-based-semantics
   // Table 3.3 Datatypes of the OWL 2 RDF-Based Semantics

   // http://www.w3.org/TR/owl2-syntax/
   // Table 3 Reserved VOcabulary of OWL 2 with Special Treatment

   import ops.*
   import XSD.this.ops.Literal.*

   val anyURI             = apply("anyURI")
   val base64Binary       = apply("base64Binary")
   val boolean            = apply("boolean")
   val `true`             = "true" ^^ boolean
   val `false`            = "false" ^^ boolean
   val byte               = apply("byte")
   val dateTime           = apply("dateTime")
   val dateTimeStamp      = apply("dateTimeStamp")
   val decimal            = apply("decimal")
   val double             = apply("double")
   val float              = apply("float")
   val hexBinary          = apply("hexBinary")
   val int                = apply("int")
   val integer            = apply("integer")
   val language           = apply("language")
   val long               = apply("long")
   val maxExclusive       = apply("maxExclusive")
   val maxInclusive       = apply("maxInclusive")
   val maxLength          = apply("maxLength")
   val minExclusive       = apply("minExclusive")
   val minInclusive       = apply("minInclusive")
   val minLength          = apply("minLength")
   val Name               = apply("Name")
   val NCName             = apply("NCName")
   val negativeInteger    = apply("negativeInteger")
   val NMToken            = apply("NMToken")
   val nonNegativeInteger = apply("nonNegativeInteger")
   val nonPositiveInteger = apply("nonPositiveInteger")
   val normalizedString   = apply("normalizedString")
   val pattern            = apply("pattern")
   val PlainLiteral       = apply("PlainLiteral")
   val positiveInteger    = apply("positiveInteger")
   val short              = apply("short")
   val string             = apply("string")
   val token              = apply("token")
   val unsignedByte       = apply("unsignedByte")
   val unsignedInt        = apply("unsignedInt")
   val unsignedLong       = apply("unsignedLong")
   val unsignedShort      = apply("unsignedShort")
end XSD
