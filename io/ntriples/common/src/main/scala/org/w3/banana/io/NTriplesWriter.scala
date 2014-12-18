package org.w3.banana.io

import java.io.OutputStream

import org.w3.banana.{syntax, RDF, RDFOps}

import scala.util.Try

/**
 * Generic NTriplesWriter
 * @param ops implicit Rdf operations, that by default are resolved from Rdf typeclass
 * @tparam Rdf class with Rdf types
 */
class NTriplesWriter[Rdf <: RDF](implicit val ops:RDFOps[Rdf]) extends RDFWriter[Rdf,Try,NTriples]  {
  import ops._

  protected def tripleAsString(triple: Rdf#Triple):String = {
    val (subject,property,objectt) = ops.fromTriple(triple)
    node2Str(subject)+" "+node2Str(property)+" "+node2Str(objectt)+" ."
  }

  /**
   * Translates node to its ntriples string representation
   * @param node Rdf node
   * @return
   */
  def node2Str(node:Rdf#Node): String = ops.foldNode(node)(
    { case ops.URI(url) => "<" + url + ">"},
    { case ops.BNode(id) => "_:" + id},
    {
      case ops.Literal((string, ops.URI(datatype), Some(lang))) => "\"" + string + "\"" + "@" + lang
      case ops.Literal((string, ops.URI(datatype), None)) => "\"" + string + "\"" + "^^<" + datatype + ">"
    }
  )

  def write(graph: Rdf#Graph, os: OutputStream, base: String): Try[Unit] = Try {
    for (triple <- graph.triples) {
      val line = tripleAsString(triple) + "\r\n"
      os.write(line.getBytes("UTF-8"))
    }
  }

  def asString(graph: Rdf#Graph, base: String): Try[String] = Try{
    (
      for ( triple <- ops.getTriples(graph))
      yield tripleAsString(triple)
    ).mkString("\r\n")
  }
}