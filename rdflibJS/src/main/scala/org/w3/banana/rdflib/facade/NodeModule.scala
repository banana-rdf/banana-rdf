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

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object nodeMod:
   import run.cosy.rdfjs.model.Term
   type ValueTypeBanana = Term[?] | Number | js.UndefOr[Nothing]
   val fromValue: js.Function1[ValueTypeBanana, ValueTypeBanana] = (any: ValueTypeBanana) =>
     any

   @JSImport("rdflib/lib/node", JSImport.Default)
   @js.native
   abstract class default protected ()
       extends js.Any //		extends types.rdflib.nodeInternalMod.default
       :
      /* protected */
      def this(value: String) = this()
   /* static members */
   object default:
      // we update the default implementation to be a no-op
      // todo deal with collections
      this.^.asInstanceOf[js.Dynamic].updateDynamic("fromValue")(nodeMod.fromValue)

      @JSImport("rdflib/lib/node", JSImport.Default)
      @js.native
      val ^ : js.Any = js.native

      // inline def fromValue[T /* <: FromValueReturns[js.Any] */](value: ValueType): T = ^.asInstanceOf[js.Dynamic].applyDynamic("fromValue")(value.asInstanceOf[js.Any]).asInstanceOf[T]

      inline def toJS(term: js.Any)
          : js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number | String |
            Boolean | js.Object]) = ^.asInstanceOf[js.Dynamic].applyDynamic("toJS")(
        term.asInstanceOf[js.Any]
      ).asInstanceOf[js.Date | Number | String | Boolean | js.Object | (js.Array[js.Date | Number |
        String | Boolean | js.Object])]
