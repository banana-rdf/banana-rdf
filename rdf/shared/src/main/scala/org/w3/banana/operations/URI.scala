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

import scala.util.Try
import org.w3.banana.RDF

object URI:
   val xsdStr: String     = "http://www.w3.org/2001/XMLSchema#string"
   val xsdLangStr: String = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"

trait URI[Rdf <: RDF]:
   /** (can) throw an exception (depending on implementation of URI) different implementations
     * decide to parse at different points, and do varying quality jobs at that (check). Need to
     * look at how capability based exceptions could help
     * https://github.com/lampepfl/dotty/pull/11721/files
     */
   def apply(uriStr: String): RDF.URI[Rdf] = mkUri(uriStr).get
   def mkUri(iriStr: String): Try[RDF.URI[Rdf]]
   protected def asString(uri: RDF.URI[Rdf]): String
   extension (uri: RDF.URI[Rdf])
      def value: String                     = asString(uri)
      def ===(other: RDF.URI[Rdf]): Boolean = uri.equals(other)
end URI