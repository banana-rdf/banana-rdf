package org.w3.banana.rd4j.io

import java.io._

import org.w3.banana.Prefix
import org.w3.banana.io._
import org.w3.banana.rd4j.{Rdf4j, Rdf4jOps}

import scala.util._

class Rdf4jRDFWriter[T](implicit
  ops: Rdf4jOps,
  rdf4jSyntax: Rdf4jSyntax[T]
) extends RDFWriter[Rdf4j, Try, T] {

  def write(graph: Rdf4j#Graph, os: OutputStream, base: String, prefixes: Set[Prefix[Rdf4j]]): Try[Unit] = Try {
    val sWriter = rdf4jSyntax.rdfWriter(os, base)
    prefixes.foreach(p => {
      sWriter.handleNamespace(p.prefixName, p.prefixIri)
    })
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
  }

  def asString(graph: Rdf4j#Graph, base: String, prefixes: Set[Prefix[Rdf4j]]): Try[String] = Try {
    val result = new StringWriter()
    val sWriter = rdf4jSyntax.rdfWriter(result, base)
    prefixes.foreach(p => {
      sWriter.handleNamespace(p.prefixName, p.prefixIri)
    })
    sWriter.startRDF()
    ops.getTriples(graph) foreach sWriter.handleStatement
    sWriter.endRDF()
    result.toString
  }
}

class Rdf4jRDFWriterHelper(implicit ops: Rdf4jOps) {

  implicit val rdfxmlWriter: RDFWriter[Rdf4j, Try, RDFXML] = new Rdf4jRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Rdf4j, Try, Turtle] = new Rdf4jRDFWriter[Turtle]

  implicit val jsonldCompactedWriter: RDFWriter[Rdf4j, Try, JsonLdCompacted] = new Rdf4jRDFWriter[JsonLdCompacted]

  implicit val jsonldExpandedWriter: RDFWriter[Rdf4j, Try, JsonLdExpanded] = new Rdf4jRDFWriter[JsonLdExpanded]

  implicit val jsonldFlattenedWriter: RDFWriter[Rdf4j, Try, JsonLdFlattened] = new Rdf4jRDFWriter[JsonLdFlattened]

}
