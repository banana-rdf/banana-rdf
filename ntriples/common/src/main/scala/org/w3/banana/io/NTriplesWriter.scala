package org.w3.banana.io

import java.io.OutputStream

import org.w3.banana.{RDF, RDFOps}

import scala.util.Try

/**
 * Generic NTriplesWriter
 * @param ops implicit Rdf operations, that by default are resolved from Rdf typeclass
 * @tparam Rdf class with Rdf types
 */
class NTriplesWriter[Rdf <: RDF](implicit val ops:RDFOps[Rdf]) extends RDFWriter[Rdf,Try,NTriples]  {
  protected def tripleAsString(subject:Rdf#Node,property:Rdf#URI,objectt:Rdf#Node):String = {
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
      case ops.Literal((string, ops.URI(datatype), None)) if ops.isLiteral(node) => "\"" + string + "\"" + "^^<" + datatype + ">"
    }
  )

  override def write(graph: Rdf#Graph, os: OutputStream, base: String): Try[Unit] = asString(graph,base).map{
    string=> os.write(string.getBytes("UTF-8"))
  }

  override def asString(graph: Rdf#Graph, base: String): Try[String] = Try{
    (
      for { trip <- ops.getTriples(graph)
            (subject,property,objectt) = ops.fromTriple(trip)
      } yield tripleAsString(subject,property,objectt)
      ).mkString("\n")
  }
}