package org.w3.rdf

import java.io._

import scalaz.Validation
import scalaz.Validation._

abstract class TurtleWriter[Rdf <: RDF](val ops: RDFOperations[Rdf]) {
  
  import ops._
  
  def write(graph: Rdf#Graph, os: OutputStream, base: String): Validation[Throwable, Unit]
  
  def write(graph: Rdf#Graph, writer: Writer, base: String): Validation[Throwable, Unit]
  
  def write(graph: Rdf#Graph, file: File, base: String): Validation[Throwable, Unit] = fromTryCatch {
    val fos = new BufferedOutputStream(new FileOutputStream(file))
    write(graph, fos, base)
  }
  
  def asString(graph: Rdf#Graph, base: String): Validation[Throwable, String] = fromTryCatch {
    val stringWriter = new StringWriter
    write(graph, stringWriter, base)
    stringWriter.toString
  }
  
}