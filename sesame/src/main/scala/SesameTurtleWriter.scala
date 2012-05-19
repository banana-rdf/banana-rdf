package org.w3.banana.sesame

import org.w3.banana._
import java.io._
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.openrdf.model.URI
import org.openrdf.model.impl.URIImpl

import scalaz.Validation
import scalaz.Validation._

object SesameTurtleWriter extends TurtleWriter[Sesame](SesameOperations) {
  
  import SesameOperations._
  
  // Sesame's parser does not handle relative URI, but let us override the behavior :-)
  def write(uri: URI, writer: Writer, baseURI: String) = {
    val uriString = uri.toString
    val uriToWrite =
      if (uriString startsWith baseURI)
        uriString.substring(baseURI.length)
      else
        uriString
      writer.write("<"+uriToWrite+">")
  }
  
  class TurtleWriterOS(os: OutputStream, baseURI: String) extends STurtleWriter(os) {
    override def writeURI(uri: URI): Unit = write(uri, writer, baseURI)
  }
  
  class TurtleWriterW(w: Writer, baseURI: String) extends STurtleWriter(w) {
    override def writeURI(uri: URI): Unit = write(uri, writer, baseURI)
  }
  
  private def write(graph: Sesame#Graph, turtleWriter: STurtleWriter, base: String): Validation[BananaException, Unit] = WrappedThrowable.fromTryCatch {
    turtleWriter.startRDF()
    graph foreach turtleWriter.handleStatement
    turtleWriter.endRDF()
  }
  
  def write(graph: Sesame#Graph, os: OutputStream, base: String): Validation[BananaException, Unit] =
    for {
      turtleWriter <- WrappedThrowable.fromTryCatch { new TurtleWriterOS(os, base) }
      result <- write(graph, turtleWriter, base)
    } yield result
  
  def write(graph: Sesame#Graph, writer: Writer, base: String): Validation[BananaException, Unit] =
    for {
      turtleWriter <- WrappedThrowable.fromTryCatch { new TurtleWriterW(writer, base) }
      result <- write(graph, turtleWriter, base)
    } yield result
  
}
