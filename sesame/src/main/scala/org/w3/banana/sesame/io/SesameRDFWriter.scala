package org.w3.banana.sesame.io

import java.io._

import org.openrdf.rio
import org.w3.banana.RDFWriterSelector
import org.w3.banana.io._
import org.w3.banana.sesame.{Sesame, SesameOps}
import scala.collection.JavaConversions._
import scala.util._

class SesameRDFWriter[T: Syntax](implicit
  ops: SesameOps,
  sesameSyntax: SesameSyntax[T]
) extends RDFWriter[Sesame, Try, T] {

  /**
   * Sort function that sorts triplets in order to produce nice looking Turtle
   * @param a first triplet
   * @param b second triplet
   * @return false if order change is required
   */
  protected def sortTriple(a:Sesame#Triple,b:Sesame#Triple) = if(a.getSubject==b.getSubject)
    a.getPredicate.stringValue < b.getPredicate.stringValue else a.getSubject.stringValue < b.getSubject.stringValue

  /**
   * Writes Sesame graph with turtle writer
   * @param graph
   * @param sWriter
   */
  protected def writeGraph(graph:Sesame#Graph,sWriter:rio.RDFWriter) = {
    sWriter.startRDF()
    for(n <- graph.getNamespaces) sWriter.handleNamespace(n.getPrefix,n.getName)
    val trips = ops.getTriples(graph).toStream.sortWith(sortTriple)
    trips foreach sWriter.handleStatement
    sWriter.endRDF()
  }

  def write(graph: Sesame#Graph, os: OutputStream, base: String): Try[Unit] =
    Try (    writeGraph(graph, sesameSyntax.rdfWriter(os, base)) )

  def asString(graph: Sesame#Graph, base: String): Try[String] = Try {
    val result = new StringWriter()
    val sWriter = sesameSyntax.rdfWriter(result, base)
    this.writeGraph(graph,sWriter)
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
