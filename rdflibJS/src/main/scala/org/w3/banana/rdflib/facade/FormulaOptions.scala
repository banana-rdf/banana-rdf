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

package org.w3.banana.rdflib.facade

import org.scalablytyped.runtime.StObject
import run.cosy.rdfjs.model.{DataFactory, Quad}

import scala.scalajs.js

object FormulaOpts:

   trait FormulaOpts extends StObject:
      var dataCallback: js.UndefOr[js.Function1[Quad, Unit]] = js.undefined
      var rdfArrayRemove: js.UndefOr[js.Function2[js.Array[Quad], Quad, Unit]] = js.undefined

      def rdfFactory: js.UndefOr[DataFactory] = js.undefined

   def apply(): FormulaOpts =
      val __obj = js.Dynamic.literal()
      __obj.asInstanceOf[FormulaOpts]

   extension [Self <: FormulaOpts](x: Self)
      def setDataCallback(value: Quad => Unit): Self =
        StObject.set(x, "dataCallback", js.Any.fromFunction1(value))
      def unsetCallback: Self = StObject.set(x, "dataCallback", js.undefined)
      def setRdfArrayRemove(
          value: ( /* arr */ js.Array[Quad], /* q */ Quad) => Unit
      ): Self = StObject.set(x, "rdfArrayRemove", js.Any.fromFunction2(value))

      def unsetArrayRemove: Self = StObject.set(x, "rdfArrayRemove", js.undefined)
      def setRdfFactory(value: DataFactory): Self =
        StObject.set(x, "rdfFactory", value.asInstanceOf[js.Any])
      def unsetRdfFactory: Self = StObject.set(x, "rdfFactory", js.undefined)
