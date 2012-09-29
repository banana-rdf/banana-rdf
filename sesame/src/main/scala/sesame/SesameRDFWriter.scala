package org.w3.banana.sesame

import org.w3.banana._
import SesameDiesel._
import SesameOperations._
import scalaz.Validation
import java.io.{ Writer => jWriter, _ }
import scalax.io._
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.{ RDFWriter => sRDFWriter }

object SesameRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Sesame, T] =
    new RDFWriter[Sesame, T] {

      val syntax = _syntax

      def write[R <: jWriter](graph: Sesame#Graph, wcr: WriteCharsResource[R], base: String): BananaValidation[Unit] =
        WrappedThrowable.fromTryCatch {
          wcr.acquireAndGet { writer =>
            val sWriter = sesameSyntax.rdfWriter(writer, base)
            sWriter.startRDF()
            graph.toIterable foreach sWriter.handleStatement
            sWriter.endRDF()
          }
        }

    }

  val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter[RDFXML]

  val turtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter[Turtle]

  implicit val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle]

}
