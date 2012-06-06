package org.w3.banana.sesame

import org.w3.banana._
import java.io._
import org.openrdf.rio.RDFWriter
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.openrdf.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}
import org.openrdf.model.URI
import org.openrdf.model.impl.URIImpl

import scalaz.Validation
import scalaz.Validation._
import org.openrdf.rio.RDFWriter
import org.w3.banana.{TurtleWriter=>BananaTurtleWriter}

abstract class SesameWriter extends BlockingWriter[Sesame] {
  val ops = SesameOperations
  
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
  
  def rdfWriter(os: OutputStream, base: String): RDFWriter

  def rdfWriter(os: Writer, base: String): RDFWriter


  private def write(graph: Sesame#Graph, turtleWriter: RDFWriter, base: String): Validation[BananaException, Unit] = WrappedThrowable.fromTryCatch {
    turtleWriter.startRDF()
    graph foreach turtleWriter.handleStatement
    turtleWriter.endRDF()
  }
  
  def write(graph: Sesame#Graph, os: OutputStream, base: String): Validation[BananaException, Unit] =
    for {
      turtleWriter <- WrappedThrowable.fromTryCatch { rdfWriter(os, base) }
      result <- write(graph, turtleWriter, base)
    } yield result
  
  def write(graph: Sesame#Graph, writer: Writer, base: String): Validation[BananaException, Unit] =
    for {
      turtleWriter <- WrappedThrowable.fromTryCatch { rdfWriter(writer, base) }
      result <- write(graph, turtleWriter, base)
    } yield result
  
}

object SesameTurtleWriter extends SesameWriter with BananaTurtleWriter[Sesame]  {

  def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
    override def writeURI(uri: URI): Unit = write(uri, writer, base)
  }

  def rdfWriter(w: Writer, base: String) = new STurtleWriter(w) {
    override def writeURI(uri: URI): Unit = write(uri, writer, base)
  }
}

object SesameRdfXmlWriter extends SesameWriter with RdfXmlWriter[Sesame] {
  def rdfWriter(os: OutputStream, base: String) = new SRdfXmlWriter(os)

  def rdfWriter(w: Writer, base: String) = new SRdfXmlWriter(w)
}
