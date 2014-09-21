package org.w3.banana

import java.io.OutputStream

import scala.util.Try

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, +T] {

  def syntax: Syntax[T]

  //todo: add a method that takes a writer

  //todo: this method needs an encoding
  def write(obj: O, outputstream: OutputStream, base: String): Try[Unit]

  def asString(obj: O, base: String): Try[String]
}
