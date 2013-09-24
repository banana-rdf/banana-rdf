package org.w3.banana

import java.io.{ OutputStream, ByteArrayOutputStream }
import scala.util.Try

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, +T] {

  def syntax: Syntax[T]

  def write(obj: O, outputstream: OutputStream, base: String): Try[Unit]

  def asString(obj: O, base: String): Try[String] = Try {
    val baos = new ByteArrayOutputStream
    write(obj, baos, base)
    new String(baos.toByteArray(), "UTF-8")
  }

}
