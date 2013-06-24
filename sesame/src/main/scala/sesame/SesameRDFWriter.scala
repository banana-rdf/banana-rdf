package org.w3.banana.sesame

import org.w3.banana._
import SesameOperations._
import java.io.{ Writer => jWriter, _ }
import scalax.io._
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.{ RDFWriter => sRDFWriter }
import scala.util._

object SesameRDFWriter {

  def apply[T](implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]): RDFWriter[Sesame, T] =
    new RDFWriter[Sesame, T] {

      val syntax = _syntax

      def write[R <: jWriter](graph: Sesame#Graph, wcr: WriteCharsResource[R], base: String): Try[Unit] =
        Try {
          wcr.acquireAndGet { writer =>
            val sWriter = sesameSyntax.rdfWriter(writer, base)
            sWriter.startRDF()
            graphToIterable(graph) foreach sWriter.handleStatement
            sWriter.endRDF()
          }
        }

    }

  val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter[RDFXML]

  val turtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter[Turtle]

  val jsonldCompactedWriter: RDFWriter[Sesame, JSONLD_COMPACTED] = SesameRDFWriter[JSONLD_COMPACTED]

  val jsonldExpandedWriter: RDFWriter[Sesame, JSONLD_EXPANDED] = SesameRDFWriter[JSONLD_EXPANDED]

  val jsonldFlattenedWriter: RDFWriter[Sesame, JSONLD_FLATTENED] = SesameRDFWriter[JSONLD_FLATTENED]

  implicit val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle]

}
