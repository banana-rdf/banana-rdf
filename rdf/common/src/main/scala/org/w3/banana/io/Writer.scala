package org.w3.banana
package io

import java.io.OutputStream

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, M[_], +T] {

  //todo: add a method that takes a writer

  //todo: this method needs an encoding
  def write(obj: O, outputstream: OutputStream, base: String): M[Unit]

  def asString(obj: O, base: String): M[String]
}
