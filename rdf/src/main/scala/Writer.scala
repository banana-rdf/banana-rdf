package org.w3.banana

import java.io.{ Writer => jWriter }
import scalax.io._
import scalaz.Validation

/**
 * @tparam O the type of object that this Writer can serialize
 * @tparam T the Syntax that this Writer supports
 */
trait Writer[-O, +T] {

  def syntax: Syntax[T]

  def write[R <: jWriter](obj: O, wcr: WriteCharsResource[R], base: String): BananaValidation[Unit]

  def write[R](obj: O, outputResource: OutputResource[R], base: String): BananaValidation[Unit] =
    write(obj, outputResource.writer(Codec.UTF8), base)

  def asString(obj: O, base: String): BananaValidation[String] = {
    val stringWriter = new java.io.StringWriter
    write(obj, Resource.fromWriter(stringWriter), base).map(_ => stringWriter.toString)
  }

}
