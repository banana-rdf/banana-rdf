package org.w3.banana

import java.io._

import scalaz.{Either3, Validation}
import scalaz.Validation._


/**
 * typeclass for an RDF Writer
 *
 * @tparam Rdf
 * @tparam SyntaxType  type of serialisation to write to. Usually a phantom type, useful for type class behavior and
 *                    for aligning writers implemented with different frameworks (eg: Jena or Sesame)
 */
trait RDFBlockingWriter[Rdf <: RDF, +SyntaxType] {

    def write(graph: Rdf#Graph, os: OutputStream, base: String): Validation[BananaException, Unit]
    def write(graph: Rdf#Graph, writer: Writer, base: String): Validation[BananaException, Unit]
    def write(graph: Rdf#Graph, file: File, base: String): Validation[BananaException, Unit] =
     for{
       fos <- WrappedThrowable.fromTryCatch { new BufferedOutputStream(new FileOutputStream(file)) }
       result <-  write(graph, fos, base)
       } yield result

    def asString(graph: Rdf#Graph, base: String): Validation[BananaException, String] = {
      val stringWriter = new StringWriter
      write(graph, stringWriter, base).map(_=>stringWriter.toString)
    }
}


trait BlockingSparqlAnswerWriter[Sparql<: SPARQL, +SyntaxType] {

  def write(answers: Sparql#Solutions, os: OutputStream): Validation[BananaException, Unit]

  def write(answer: Boolean, os: OutputStream): Validation[BananaException, Unit]

// this can be placed in an object, it needs to call either a graphwriter or a sparqlwriter
// def write(answers: Either3[Sparql#Solutions, RDF#Graph, Boolean], os: OutputStream): Validation[BananaException, Unit]

}
