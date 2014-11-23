package org.w3.banana
package io

import java.io.OutputStream

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
abstract class Writer[-O, M[_], +T: Syntax] {

   val transformsTo: Syntax[T] = Syntax[T]

  //todo: add a method that takes a writer

  //todo: this method needs an encoding
  def write(obj: O, outputstream: OutputStream, base: String): M[Unit]

  def asString(obj: O, base: String): M[String]
}
