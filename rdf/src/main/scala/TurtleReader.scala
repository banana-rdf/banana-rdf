package org.w3.rdf

import java.io._

abstract class TurtleReader[RDF <: RDFDataType](val ops: RDFOperations[RDF]) {
  
  import ops._
  
  def read(is: InputStream, base: String): Either[Throwable, RDF#Graph]
  
  def read(reader: Reader, base: String): Either[Throwable, RDF#Graph]
  
  def read(file: File, base: String): Either[Throwable, RDF#Graph] =
    try {
      val fis = new BufferedInputStream(new FileInputStream(file))
      read(fis, base)
    } catch {
      case t => Left(t)
    }
  
  def read(s: String, base: String): Either[Throwable, RDF#Graph] =
    try {
      val reader = new StringReader(s)
      read(reader, base)
    } catch {
      case t => Left(t)
    }
  
}