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

import org.w3.banana.RDF
import scala.annotation.targetName

trait Literal[Rdf <: RDF](using ops: org.w3.banana.Ops[Rdf]):
   enum LiteralI(val text: String):
      case Plain(override val text: String) extends LiteralI(text)
      case `@`(override val text: String, lang: RDF.Lang[Rdf]) extends LiteralI(text)
      case ^^(override val text: String, dataTp: RDF.URI[Rdf]) extends LiteralI(text)

   def apply(plain: String): RDF.Literal[Rdf]
   def apply(lit: LiteralI): RDF.Literal[Rdf]
   def unapply(lit: Matchable): Option[LiteralI]
   @targetName("langLit")
   def apply(lex: String, lang: RDF.Lang[Rdf]): RDF.Literal[Rdf]
   @targetName("dataTypeLit")
   def apply(lex: String, dataTp: RDF.URI[Rdf]): RDF.Literal[Rdf]

   lazy val langTp: RDF.URI[Rdf] = ops.URI(org.w3.banana.operations.URI.xsdLangStr)
   lazy val stringTp: RDF.URI[Rdf] = ops.URI(org.w3.banana.operations.URI.xsdStr)

   extension (lit: RDF.Literal[Rdf])
      def text: String
      // todo: this can be implemented more efficiently in individual subclasses by
      // avoiding going through the intermdiate LiteralI type. Indeed the
      // unapply should be implemented in terms of this function
      def fold[A](
          plainF: String => A,
          langF: (String, RDF.Lang[Rdf]) => A,
          dtTypeF: (String, RDF.URI[Rdf]) => A
      ): A =
         import LiteralI.*
         unapply(lit).get match
          case Plain(t)   => plainF(t)
          case t `@` lang => langF(t, lang)
          case t ^^ dt    => dtTypeF(t, dt)
      def lang: Option[RDF.Lang[Rdf]] = lit.fold(_ => None, (_, l) => Some(l), (_, _) => None)
      def dataType: RDF.URI[Rdf] = lit.fold(_ => stringTp, (_, _) => langTp, (_, tp) => tp)

   extension (str: String)
      @targetName("dt")
      infix def ^^(dtType: RDF.URI[Rdf]): RDF.Literal[Rdf] =
        apply(str, dtType)
      @targetName("lang")
      infix def `@`(lang: RDF.Lang[Rdf]): RDF.Literal[Rdf] =
        apply(str, lang)
end Literal
