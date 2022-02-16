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

import scala.language.higherKinds
import scala.scalajs.js

/** This is the base trait for all Javascript objects in the ScalablyTyped encoding.
  *
  * An object can be freely duplicated, immutably combined with another JS object, and can be
  * mutated by setting arbitrary values on it. All of this just like in JS.
  *
  * It also has `unsafeCastN` syntax to just cast type parameters of descendant types.
  */
trait StObject extends js.Object

object StObject:
   /** Create a new empty object
     * @return
     */
   @inline
   def apply(): StObject = js.Dynamic.literal().asInstanceOf[StObject]

   /** Mutate the current object and set an arbitrary member on it.
     *
     * This is after all allowed in Javascript, and in ScalablyTyped it forms the basis of the
     * mutable builder encoding.
     */
   @inline
   def set[Self <: js.Any](x: Self, key: String, value: Any): Self =
      x.asInstanceOf[js.Dynamic].updateDynamic(key)(value.asInstanceOf[js.Any])
      x

   @inline
   implicit class StObjectOps[Self <: StObject](val x: Self) extends AnyVal:

      /** Make a copy of this object.
        *
        * Corresponds to:
        * ```typescript
        * const foo = {...bar}
        * ```
        *
        * Note that it's meant to be used with "plain" objects, some notion of class-ness may be
        * lost
        */
      @inline
      def duplicate: Self =
        js.Dynamic.global.Object.assign(js.Dynamic.literal(), x).asInstanceOf[Self]

      /** Make a copy of this object with all the members from other (the latter takes preference).
        *
        * Corresponds to:
        * ```typescript
        * const foo = {...bar}
        * ```
        *
        * Note that it's meant to be used with "plain" objects, some notion of class-ness may be
        * lost
        */
      @inline
      def combineWith[Other <: js.Any](other: Other): Self & Other =
        js.Object
          .assign(js.Dynamic.literal(), x, other.asInstanceOf[js.Object])
          .asInstanceOf[Self & Other]

      /** Mutate the current object and set an arbitrary member on it.
        *
        * This is after all allowed in Javascript, and in ScalablyTyped it forms the basis of the
        * mutable builder encoding.
        */
      @inline
      def set(key: String, value: Any): Self =
        StObject.set(x, key, value)

   @inline
   implicit class StObjectCast1Ops[Self[_] <: StObject, T1](val x: Self[T1]) extends AnyVal:

      /** Scala is more strict than Typescript, and sometimes a cast is the best solution.
        *
        * This exists to make casts safer, since it only casts the type parameters
        */
      def unsafeCast1[U1]: Self[U1] =
        x.asInstanceOf[Self[U1]]

   @inline
   implicit class StObjectCast2Ops[Self[_, _] <: StObject, T1, T2](val x: Self[T1, T2])
       extends AnyVal:

      /** Scala is more strict than Typescript, and sometimes a cast is the best solution.
        *
        * This exists to make casts safer, since it only casts the type parameters
        */
      def unsafeCast2[U1, U2]: Self[U1, U2] =
        x.asInstanceOf[Self[U1, U2]]

   @inline
   implicit class StObjectCast3Ops[Self[_, _, _] <: StObject, T1, T2, T3](val x: Self[T1, T2, T3])
       extends AnyVal:

      /** Scala is more strict than Typescript, and sometimes a cast is the best solution.
        *
        * This exists to make casts safer, since it only casts the type parameters
        */
      def unsafeCast3[U1, U2, U3]: Self[U1, U2, U3] =
        x.asInstanceOf[Self[U1, U2, U3]]
