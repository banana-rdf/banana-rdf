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
import org.w3.banana.RDF.rURI
import org.w3.banana.exceptions.*
import org.w3.banana.{Ops, RDF}

import java.net.URI as jURI
import scala.util.{Failure, Success, Try}

/** If one had a type RelURI which was definitely a relative URI, then type rURI = RelURI | URI
  *
  * The way one models this is OO programming is that rURI is a superclass of URI. I.e. every URI is
  * a rURI, but not every rURI is a URI. We can only merge two graphs when we know that it contains
  * only URIs.
  *
  * It would be too costly with the current implementations to have a RelURI type, as none of the
  * major Java RDF frameworks have such a type, and so testing for it would require parsing each
  * URL. Furthermore in order in most of those frameworks one would then immediately loose that
  * information.
  *
  * We are just concerned here to stop certain operations being possible (such as graph union). And
  * for that all we need is that if a graph contains even one suspected relative URL, then it cannot
  * be merged with another.
  */
trait rURI[Rdf <: RDF](using ops: Ops[Rdf]):
   // todo: should throw an exception or return a Try
   def apply(uriStr: String): RDF.rURI[Rdf] = mkUri(uriStr).get

   // This will create the URI with minimal verification, assuming the uriStr is already well formed
   protected def mkUriUnsafe(uriStr: String): RDF.rURI[Rdf]

   def mkUri(iriStr: String): Try[RDF.rURI[Rdf]] =
     ll.Uri.parseTry(iriStr).flatMap(_ => Try(mkUriUnsafe(iriStr)))

   protected def stringVal(uri: RDF.rURI[Rdf]): String

   /** resolve url with base, but fail if URL parsing fails, or if the Url is schema relative -
     * since that cannot be resolved.
     */
   def resolveUri(uri: RDF.rURI[Rdf], base: ll.AbsoluteUrl): Try[(RDF.URI[Rdf], Boolean)] =
     ll.Uri.parseTry(uri.value).flatMap { llUri =>
       llUri match
        case _: ll.Urn =>
          Success((uri.asInstanceOf[RDF.URI[Rdf]], false)) // urns are always absolute
        case _: ll.AbsoluteUrl   => Success((uri.asInstanceOf[RDF.URI[Rdf]], false))
        case rel: ll.RelativeUrl => Success((ops.URI(rel.resolve(base, true).toString), true))
        case nonResolvable       => Failure(URIException(s"cannot resolve $nonResolvable"))
     }

   extension (uri: RDF.rURI[Rdf])
      def value: String = stringVal(uri)

      def fragmentLess: RDF.rURI[Rdf] =
         given deflt: UriConfig = UriConfig.default
         ll.Uri.parseTry(uri.value).collect {
           case url: ll.AbsoluteUrl if url.fragment != None =>
             apply(url.copy(fragment = None).toString)
           case rurl: ll.RelativeUrl if rurl.fragment != None =>
             apply(rurl.copy(fragment = None).toString)
         }.getOrElse(uri)

      /** Returns resolved URL, and whether the result is new. If the URL is relative but cannot be
        * resolved, be lenient and keep the original. //todo: pass a logger
        * @param base
        *   the base URL to resolve against
        * @return
        *   (a resolved URL, true if the result is new)
        */
      def resolveAgainst(base: ll.AbsoluteUrl): (RDF.URI[Rdf], Boolean) =
        resolveUri(uri, base) match
         case Success(p) => p
         case Failure(e) => (uri.asInstanceOf[RDF.URI[Rdf]], false)

      /** Return the resolved URL, and whether the result is new * */
      def resolveAgainstUrl(base: ll.AbsoluteUrl): Try[(RDF.URI[Rdf], Boolean)] =
        resolveUri(uri, base)

end rURI
