package org.w3.banana.sesame

import java.io._
import org.openrdf.rio.turtle.{TurtleWriter => STurtleWriter}
import org.openrdf.rio.rdfxml.{RDFXMLWriter => SRdfXmlWriter}

import scalaz.Validation
import org.openrdf.rio.RDFWriter
import org.w3.banana._


object SesameWriter {

  import Sesame.diesel._
  import SesameOperations._

  def apply[SyntaxType](implicit sesameSyntax: SesameSyntax[SyntaxType],
                         syntax: Syntax[SyntaxType]): RDFBlockingWriter[Sesame, SyntaxType] =
    new RDFBlockingWriter[Sesame, SyntaxType] {

      private def write(graph: Sesame#Graph, rdfWriter: RDFWriter, base: String): Validation[BananaException, Unit] =
        WrappedThrowable.fromTryCatch {
          rdfWriter.startRDF()
          graph.toIterable foreach rdfWriter.handleStatement
          rdfWriter.endRDF()
        }

      def syntax[S >: SyntaxType] = syntax

      def write(graph: Sesame#Graph, os: OutputStream, base: String): Validation[BananaException, Unit] =
        for {
          rdfWriter <- WrappedThrowable.fromTryCatch {
            sesameSyntax.rdfWriter(os, base)
          }
          result <- write(graph, rdfWriter, base)
        } yield result

      def write(graph: Sesame#Graph, writer: Writer, base: String): Validation[BananaException, Unit] =
        for {
          rdfWriter <- WrappedThrowable.fromTryCatch {
            sesameSyntax.rdfWriter(writer, base)
          }
          result <- write(graph, rdfWriter, base)
        } yield result


    }

  implicit val RDFXMLWriter: RDFBlockingWriter[Sesame, RDFXML] = SesameWriter[RDFXML]

  implicit val TurtleWriter: RDFBlockingWriter[Sesame, Turtle] = SesameWriter[Turtle]

//  implicit val ReaderSelector: RDFBlockingWriter[Sesame] = ReaderSelector[Sesame, RDFXML] combineWith ReaderSelector[Sesame, Turtle]



}
