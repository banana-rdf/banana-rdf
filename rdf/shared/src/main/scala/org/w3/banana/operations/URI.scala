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

package org.w3.banana.operations

import io.lemonlabs.uri as ll
import io.lemonlabs.uri.config.UriConfig
import org.w3.banana.{Ops, RDF}
import org.w3.banana.exceptions.*

import java.net.URI as jURI
import scala.util.{Failure, Try}

object URI:
   val xsdStr: String     = "http://www.w3.org/2001/XMLSchema#string"
   val xsdLangStr: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

trait URI[Rdf <: RDF](using ops: Ops[Rdf]):
   import ops.{rURI, given}
   import scala.language.implicitConversions
   import ops.given
   // This will create the URI with minimal verification, assuming the uriStr is already well formed
   protected def mkUriUnsafe(uriStr: String): RDF.URI[Rdf]

   /** Create a new URI from string. Must throw an exception consistently if not able URI is
     * relative or malformed. Needlook at how capability based exceptions could help
     * https://github.com/lampepfl/dotty/pull/11721/files
     */
   def apply(uriStr: String): RDF.URI[Rdf] = mkUri(uriStr).get
   // todo: make the type of absoluteUrl generic, because akka urls or http4s urls could also be good candidates
   def apply(absUrl: ll.AbsoluteUrl): RDF.URI[Rdf] =
     mkUriUnsafe(absUrl.toString) // todo: or toStringRaw?
   def apply(urn: ll.Urn): RDF.URI[Rdf] = mkUriUnsafe(urn.toString)

   /** we verify the string is good using lemonlabs. Implementations may directly use implementation
     * if it gives the same results (requires serious testing)
     */
   def mkUri(iriStr: String): Try[RDF.URI[Rdf]] =
     ll.Uri.parseTry(iriStr).flatMap {
       case rel: ll.RelativeUrl =>
         Failure(URIException(s"Expected Absolute URI, but received Relative URL: $rel"))
       case prel: ll.ProtocolRelativeUrl =>
         Failure(URIException(s"Expected Absolute URI, but received protocol Relative URL: $prel"))
       case good => Try(mkUriUnsafe(good.toString))
     }

   protected def stringVal(uri: RDF.URI[Rdf]): String

   extension (uri: RDF.URI[Rdf])
      def value: String = stringVal(uri)

      // if URL then add fragment, or else return original URN
      def withFragment(frag: String): RDF.URI[Rdf] =
        ll.Uri.parseTry(uri.value).collect {
          case url: ll.Url => apply(url.withFragment(frag).toString)
        }.getOrElse(uri)

      def fragmentLess: RDF.URI[Rdf] = ll.Uri.parseTry(uri.value).collect {
        case url: ll.AbsoluteUrl if url.fragment != None =>
          given deflt: UriConfig = UriConfig.default
          apply(url.copy(fragment = None).toString)
      }.getOrElse(uri)

      def baseFor(rel: ll.RelativeUrl): RDF.URI[Rdf] =
        ll.Uri.parse(uri.value) match
           case us: ll.UrlWithScheme => apply(rel.resolve(us, true).toString)
           case _                    => uri

      // def value: String <- we use rURI implementation everywhere
      /** return _1 the relativized url relativized _2 if a change was made true else false todo:
        * follow https://github.com/lemonlabsuk/scala-uri/issues/466
        */
      def relativizeAgainst(base: ll.AbsoluteUrl): (RDF.rURI[Rdf], Boolean) =
         val juri1: jURI   = new jURI(uri.value)
         val juri2: jURI   = juri1.normalize().nn
         val baseJuri      = new jURI(base.normalize().toString)
         val relJuri: jURI = baseJuri.relativize(juri2).nn
         if (juri2 eq relJuri) && (juri1 eq juri2) then (uri.asInstanceOf[RDF.rURI[Rdf]], false)
         else (rURI(relJuri.toString), true)

      def ===(other: RDF.URI[Rdf]): Boolean = uri.equals(other)
end URI
