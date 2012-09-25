package org.w3.banana.sesame

import org.w3.banana._
import SesameDiesel._
import SesameOperations._
import scalaz.Validation
import java.io.{ Writer => jWriter, _ }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.{ RDFWriter => sRDFWriter }

object SesameRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Sesame, T] =
    new RDFWriter[Sesame, T] {

      val syntax = _syntax

      private def write(graph: Sesame#Graph, rdfWriter: sRDFWriter, base: String): BananaValidation[Unit] =
        WrappedThrowable.fromTryCatch {
          rdfWriter.startRDF()
          graph.toIterable foreach rdfWriter.handleStatement
          rdfWriter.endRDF()
        }

      def write(graph: Sesame#Graph, os: OutputStream, base: String): BananaValidation[Unit] =
        for {
          rdfWriter <- WrappedThrowable.fromTryCatch {
            sesameSyntax.rdfWriter(os, base)
          }
          result <- write(graph, rdfWriter, base)
        } yield result

      def write(graph: Sesame#Graph, writer: jWriter, base: String): BananaValidation[Unit] =
        for {
          rdfWriter <- WrappedThrowable.fromTryCatch {
            sesameSyntax.rdfWriter(writer, base)
          }
          result <- write(graph, rdfWriter, base)
        } yield result

    }

  val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter[RDFXML]

  val turtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter[Turtle]

  implicit val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle]

}
