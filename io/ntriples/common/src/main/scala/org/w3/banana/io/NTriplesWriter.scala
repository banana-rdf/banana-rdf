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
  import ops._

  protected def tripleAsString(triple: Rdf#Triple, base: Rdf#URI):String = {
    val (subject,property,objectt) = ops.fromTriple(triple)
    node2Str(subject,base)+" "+node2Str(property,base)+" "+node2Str(objectt,base)+" ."
  }

  /**
   * Translates node to its ntriples string representation
   * @param node Rdf node
   * @return
   */
  def node2Str(node:Rdf#Node, base: Rdf#URI): String = ops.foldNode(node)(
    uri => "<" + asString(uri,base) + ">",
    { case ops.BNode(id) => "_:" + id },
    _ match {
      case ops.Literal((string, _, Some(lang))) => "\"" + string + "\"" + "@" + lang
      case ops.Literal((string, datatype, None)) => "\"" + string + "\"" + "^^<" + asString(datatype,base).toString + ">"
    }
  )

  def asString(uri: Rdf#URI, base: Rdf#URI): String =
    base.resolve(uri) match {
      case ops.URI(uri) => uri
    }


  def write(graph: Rdf#Graph, os: OutputStream, base: String): Try[Unit] = Try {
    val baseUri = ops.URI(base)
    for (triple <- graph.triples) {
      val line = tripleAsString(triple,baseUri) + "\r\n"
      os.write(line.getBytes("UTF-8"))
    }
  }

  def asString(graph: Rdf#Graph, base: String): Try[String] = Try{
    val baseUri = ops.URI(base)
    (
      for ( triple <- ops.getTriples(graph))
      yield tripleAsString(triple,baseUri)
    ).mkString("\r\n")
  }
}