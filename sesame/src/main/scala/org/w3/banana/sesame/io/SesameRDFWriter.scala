package org.w3.banana.sesame.io

import java.io._

import org.w3.banana.RDFWriterSelector
import org.w3.banana.io._
import org.w3.banana.sesame.{Sesame, SesameOps}

import scala.util._

class SesameRDFWriter[T: Syntax](implicit
  ops: SesameOps,
  sesameSyntax: SesameSyntax[T]
) extends RDFWriter[Sesame, Try, T] {

  def write(graph: Sesame#Graph, os: OutputStream, base: String): Try[Unit] = Try {
    val sWriter = sesameSyntax.rdfWriter(os, base)
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
  }

  def asString(graph: Sesame#Graph, base: String): Try[String] = Try {
    val result = new StringWriter()
    val sWriter = sesameSyntax.rdfWriter(result, base)
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
    result.toString
  }
}

class SesameRDFWriterHelper(implicit ops: SesameOps) {

  implicit val rdfxmlWriter: RDFWriter[Sesame, Try, RDFXML] = new SesameRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Sesame, Try, Turtle] = new SesameRDFWriter[Turtle]

  implicit val jsonldCompactedWriter: RDFWriter[Sesame, Try, JsonLdCompacted] = new SesameRDFWriter[JsonLdCompacted]

  implicit val jsonldExpandedWriter: RDFWriter[Sesame, Try, JsonLdExpanded] = new SesameRDFWriter[JsonLdExpanded]

  implicit val jsonldFlattenedWriter: RDFWriter[Sesame, Try, JsonLdFlattened] = new SesameRDFWriter[JsonLdFlattened]

  val selector: RDFWriterSelector[Sesame, Try] =
    WriterSelector[Sesame#Graph, Try, RDFXML] combineWith
    WriterSelector[Sesame#Graph, Try, Turtle] combineWith
    WriterSelector[Sesame#Graph, Try, JsonLdCompacted] combineWith
    WriterSelector[Sesame#Graph, Try, JsonLdExpanded] combineWith
    WriterSelector[Sesame#Graph, Try, JsonLdFlattened]

}
