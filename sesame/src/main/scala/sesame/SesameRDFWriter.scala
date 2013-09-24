package org.w3.banana.sesame

import org.w3.banana._
import SesameOperations._
import java.io.{ Writer => jWriter, _ }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.{ RDFWriter => sRDFWriter }
import scala.util._

object SesameRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Sesame, T] =
    new RDFWriter[Sesame, T] {

      val syntax = _syntax

      def write(graph: Sesame#Graph, os: OutputStream, base: String): Try[Unit] = Try {
        val sWriter = sesameSyntax.rdfWriter(os, base)
        sWriter.startRDF()
        graphToIterable(graph) foreach sWriter.handleStatement
        sWriter.endRDF()
      }

    }

  val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter[RDFXML]

  val turtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter[Turtle]

  implicit val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle]

}
