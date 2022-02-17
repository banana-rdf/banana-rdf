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

package org.scalablytyped.runtime

import scala.scalajs.js
import scala.scalajs.js.WrappedDictionary

import scala.language.implicitConversions

trait StringDictionary[+V] extends StObject

object StringDictionary:

   /** Returns a new empty dictionary */
   @inline def empty[A]: StringDictionary[A] =
     (new js.Object).asInstanceOf[StringDictionary[A]]

   @inline
   def apply[A](properties: (String, A)*): StringDictionary[A] =
     js.special.objectLiteral(properties*).asInstanceOf[StringDictionary[A]]

   @inline implicit def wrapStringDictionary[V](dict: StringDictionary[V]): WrappedDictionary[V] =
     new WrappedDictionary(dict.asInstanceOf[js.Dictionary[V]])
