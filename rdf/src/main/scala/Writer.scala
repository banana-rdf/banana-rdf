package org.w3.banana

import java.io.{ Writer => jWriter, _ }

import scalaz.Validation

trait Writer[-O, +T] {

  def syntax: Syntax[T]

  def write(obj: O, os: OutputStream, base: String): BananaValidation[Unit]

  def write(obj: O, writer: jWriter, base: String): BananaValidation[Unit]

  def write(obj: O, file: File, base: String): BananaValidation[Unit] = {
    for {
      fos <- WrappedThrowable.fromTryCatch { new BufferedOutputStream(new FileOutputStream(file)) }
      result <- write(obj, fos, base)
    } yield result
  }

  def asString(obj: O, base: String): BananaValidation[String] = {
    val stringWriter = new StringWriter
    write(obj, stringWriter, base).map(_ => stringWriter.toString)
  }

}
