package org.w3.banana.sesame

import java.io.{ Writer => jWriter, _ }

import org.openrdf.rio.rdfxml.{ RDFXMLWriter => SRdfXmlWriter }
import org.openrdf.rio.turtle.{ TurtleWriter => STurtleWriter }
import org.openrdf.rio.{ RDFWriter => sRDFWriter }
import org.w3.banana._

import scala.util._

class SesameRDFWriter[T](ops: SesameOps)(implicit sesameSyntax: SesameSyntax[T], _syntax: Syntax[T]) extends RDFWriter[Sesame, T] {

  val syntax = _syntax

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

  implicit val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = new SesameRDFWriter[RDFXML](ops)

  implicit val turtleWriter: RDFWriter[Sesame, Turtle] = new SesameRDFWriter[Turtle](ops)

  implicit val jsonldCompactedWriter: RDFWriter[Sesame, JSONLD_COMPACTED] = new SesameRDFWriter[JSONLD_COMPACTED](ops)

  implicit val jsonldExpandedWriter: RDFWriter[Sesame, JSONLD_EXPANDED] = new SesameRDFWriter[JSONLD_EXPANDED](ops)

  implicit val jsonldFlattenedWriter: RDFWriter[Sesame, JSONLD_FLATTENED] = new SesameRDFWriter[JSONLD_FLATTENED](ops)

  val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle] combineWith
      RDFWriterSelector[Sesame, JSONLD_COMPACTED] combineWith RDFWriterSelector[Sesame, JSONLD_EXPANDED] combineWith
      RDFWriterSelector[Sesame, JSONLD_FLATTENED]

}
