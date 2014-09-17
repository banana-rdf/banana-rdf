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

  val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = new SesameRDFWriter[RDFXML](ops)

  val turtleWriter: RDFWriter[Sesame, Turtle] = new SesameRDFWriter[Turtle](ops)

  val selector: RDFWriterSelector[Sesame] =
    RDFWriterSelector[Sesame, RDFXML] combineWith RDFWriterSelector[Sesame, Turtle]

}
