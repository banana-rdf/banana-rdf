package org.w3.rdf

import java.io._

abstract class TurtleWriter[M <: RDFModule](val m: M) {
  
  import m._
  
  def write(graph: Graph, os: OutputStream, base: String): Either[Throwable, Unit]
  
  def write(graph: Graph, writer: Writer, base: String): Either[Throwable, Unit]
  
  def write(graph: Graph, file: File, base: String): Either[Throwable, Unit] =
    try {
      val fos = new BufferedOutputStream(new FileOutputStream(file))
      write(graph, fos, base)
      Right()
    } catch {
      case t => Left(t)
    }
  
  def asString(graph: Graph, base: String): Either[Throwable, String] =
    try {
      val stringWriter = new StringWriter
      write(graph, stringWriter, base)
      Right(stringWriter.toString)
    } catch {
      case t => Left(t)
    }
  
}