package org.w3.rdf

import java.io._

abstract class TurtleReader[M <: RDFModule](val m: M) {
  
  import m._
  
  def read(is: InputStream, base: String): Either[Throwable, Graph]
  
  def read(reader: Reader, base: String): Either[Throwable, Graph]
  
  def read(file: File, base: String): Either[Throwable, Graph] =
    try {
      val fis = new BufferedInputStream(new FileInputStream(file))
      read(fis, base)
    } catch {
      case t => Left(t)
    }
  
  def read(s: String, base: String): Either[Throwable, Graph] =
    try {
      val reader = new StringReader(s)
      read(reader, base)
    } catch {
      case t => Left(t)
    }
  
}