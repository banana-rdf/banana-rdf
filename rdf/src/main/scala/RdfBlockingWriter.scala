package org.w3.banana

import java.io._

import scalaz.Validation

/**
 * typeclass for an RDF Writer that is blocking.
 *
 * @tparam Rdf
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait RDFBlockingWriter[Rdf <: RDF, +SyntaxType] extends BlockingWriter[Rdf#Graph, SyntaxType] {

  def write(graph: Rdf#Graph, os: OutputStream, base: String): Validation[BananaException, Unit]
  def write(graph: Rdf#Graph, writer: Writer, base: String): Validation[BananaException, Unit]
  def write(graph: Rdf#Graph, file: File, base: String): Validation[BananaException, Unit] =
    for {
      fos <- WrappedThrowable.fromTryCatch { new BufferedOutputStream(new FileOutputStream(file)) }
      result <- write(graph, fos, base)
    } yield result

  def asString(graph: Rdf#Graph, base: String): Validation[BananaException, String] = {
    val stringWriter = new StringWriter
    write(graph, stringWriter, base).map(_ => stringWriter.toString)
  }
}

object RDFBlockingWriter {

  def apply[Rdf <: RDF, SyntaxType](implicit rdfWriter: RDFBlockingWriter[Rdf, SyntaxType]): RDFBlockingWriter[Rdf, SyntaxType] = rdfWriter

}
