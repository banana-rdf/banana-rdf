package org.w3.banana

import java.io.{ Writer => jWriter }
import scalax.io._
import scala.util.Try

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, +T] {

  def syntax: Syntax[T]

  def write[R <: jWriter](obj: O, wcr: WriteCharsResource[R], base: String): Try[Unit]

  def write[R](obj: O, outputResource: OutputResource[R], base: String): Try[Unit] =
    write(obj, outputResource.writer(Codec.UTF8), base)

  def asString(obj: O, base: String): Try[String] = {
    val stringWriter = new java.io.StringWriter
    write(obj, Resource.fromWriter(stringWriter), base).map(_ => stringWriter.toString)
  }

}
