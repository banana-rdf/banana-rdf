package org.w3.banana.jena

import java.io.{ Writer, OutputStream }
import org.w3.banana._
import scalaz.Validation

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter {

  def apply[T](implicit jenaSparqlSyntax: SparqlAnswerOut[T], _syntax: Syntax[T]): SPARQLSolutionsWriter[Jena, T] =
    new SPARQLSolutionsWriter[Jena, T] {

      val syntax = _syntax

      def write(answers: Jena#Solutions, os: OutputStream, base: String) =
        WrappedThrowable.fromTryCatch {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def write(input: Jena#Solutions, writer: Writer, base: String) = null

    }

  implicit val Json: SPARQLSolutionsWriter[Jena, SparqlAnswerJson] =
    SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML: SPARQLSolutionsWriter[Jena, SparqlAnswerXML] =
    SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val writerSelector: SPARQLSolutionsWriterSelector[Jena] =
    SPARQLSolutionWriterSelector[Jena, SparqlAnswerXML] combineWith
      SPARQLSolutionWriterSelector[Jena, SparqlAnswerXML]

}
