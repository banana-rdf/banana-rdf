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

import org.w3.banana.RDF.*

import scala.util.*

trait Prefix[Rdf <: RDF](using Ops[Rdf]):
   def prefixName: String
   def prefixIri: RDF.URI[Rdf]
   def apply(value: String): URI[Rdf]
   def unapply(iri: URI[Rdf]): Option[String]
end Prefix

object Prefix:
   def apply[Rdf <: RDF](
       prefixName: String,
       prefixIri: String
   )(using ops: Ops[Rdf]): Prefix[Rdf] =
     new PrefixBuilder[Rdf](prefixName, ops.URI(prefixIri))
end Prefix

// todo: should we have a version with a relativePrefixIRI that creates rIRIs?
open class PrefixBuilder[Rdf <: RDF](
    val prefixName: String,
    val prefixIri: RDF.URI[Rdf]
)(using ops: Ops[Rdf]) extends Prefix[Rdf]:
   import ops.{*, given}

   override def toString: String = "Prefix(" + prefixName + ")"
   lazy val prefixVal = prefixIri.value

   def apply(value: String): RDF.URI[Rdf] = ops.URI(prefixIri.value + value)

   def unapply(iri: RDF.URI[Rdf]): Option[String] =
      val uriString: String = iri.value
      if uriString.startsWith(prefixVal) then
         Some(uriString.substring(prefixVal.length).nn)
      else
         None

   def getLocalName(iri: URI[Rdf]): Try[String] = unapply(iri) match
    case Some(localname) => Success(localname)
    case _: None.type =>
      Failure(Exception(this.toString + " couldn't extract localname for " + iri))

end PrefixBuilder
