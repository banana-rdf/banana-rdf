package org.w3.rdf

import java.io._

abstract class TurtleReader[Rdf <: RDF](val ops: RDFOperations[Rdf]) {
  
  import ops._
  
  def read(is: InputStream, base: String): Either[Throwable, Rdf#Graph]
  
  def read(reader: Reader, base: String): Either[Throwable, Rdf#Graph]
  
  def read(file: File, base: String): Either[Throwable, Rdf#Graph] =
    try {
      val fis = new BufferedInputStream(new FileInputStream(file))
      read(fis, base)
    } catch {
      case t => Left(t)
    }

  def read(file: File, base: String, encoding: String): Either[Throwable, Rdf#Graph] =
    try {
      val fis = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)),encoding)
      read(fis, base)
    } catch {
      case t => Left(t)
    }

  def read(s: String, base: String): Either[Throwable, Rdf#Graph] =
    try {
      val reader = new StringReader(s)
      read(reader, base)
    } catch {
      case t => Left(t)
    }
}