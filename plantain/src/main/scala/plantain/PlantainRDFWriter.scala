package org.w3.banana.plantain

import org.w3.banana._
import java.io.{ Writer => jWriter, _ }
import scala.util._

object PlantainRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Plantain, T] =
    new RDFWriter[Plantain, T] {

      val syntax = _syntax

      def write[R <: jWriter](graph: Plantain#Graph, os: WriteCharsResource[R], base: String): Try[Unit] =
        Try {
          wcr.acquireAndGet { writer =>
            val sWriter = sesameSyntax.rdfWriter(writer, base)
            sWriter.startRDF()
            graph.triples foreach { triple => sWriter.handleStatement(triple.asSesame) }
            sWriter.endRDF()
          }
        }

    }

  implicit val rdfxmlWriter: RDFWriter[Plantain, RDFXML] = PlantainRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainRDFWriter[Turtle]

  implicit val selector: RDFWriterSelector[Plantain] =
    RDFWriterSelector[Plantain, RDFXML] combineWith RDFWriterSelector[Plantain, Turtle]

}


/* copied from the banana-sesame codebase to avoid the dependency */

import org.openrdf.rio.RDFWriter
import java.io.{ Writer, OutputStream }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.model.{ URI => SesameURI }

/**typeclass that reflects a Jena String that can be used to construct a BlockingReader */
trait SesameSyntax[T] {
  def rdfWriter(os: OutputStream, base: String): RDFWriter
  def rdfWriter(wr: Writer, base: String): RDFWriter
}

object SesameSyntax {

  implicit val RDFXML: SesameSyntax[RDFXML] = new SesameSyntax[RDFXML] {
    def rdfWriter(os: OutputStream, base: String) = new SRdfXmlWriter(os)

    def rdfWriter(wr: Writer, base: String) = new SRdfXmlWriter(wr)
  }

  implicit val Turtle: SesameSyntax[Turtle] = new SesameSyntax[Turtle] {
    // Sesame's parser does not handle relative URI, but let us override the behavior :-)
    def write(uri: SesameURI, writer: Writer, baseURI: String) = {
      val uriString = uri.toString
      val uriToWrite =
        if (uriString startsWith baseURI)
          uriString.substring(baseURI.length)
        else
          uriString
      writer.write("<" + uriToWrite + ">")
    }

    def rdfWriter(os: OutputStream, base: String) = new STurtleWriter(os) {
      override def writeURI(uri: SesameURI): Unit = write(uri, writer, base)
    }

    def rdfWriter(wr: Writer, base: String) = new STurtleWriter(wr) {
      override def writeURI(uri: SesameURI): Unit = write(uri, writer, base)
    }
  }

}
