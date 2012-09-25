package org.w3.banana.jena

import java.io.{ Writer, OutputStream }
import org.w3.banana._
import scalaz.Validation

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object JenaSolutionsWriter {

  def apply[T](implicit jenaSparqlSyntax: JenaAnswerOutput[T], _syntax: Syntax[T]): SparqlSolutionsWriter[Jena, T] =
    new SparqlSolutionsWriter[Jena, T] {

      val syntax = _syntax

      def write(answers: Jena#Solutions, os: OutputStream, base: String) =
        WrappedThrowable.fromTryCatch {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def write(input: Jena#Solutions, writer: Writer, base: String) = null

    }

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter[SparqlAnswerXml]

  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] =
    SPARQLSolutionWriterSelector[Jena, SparqlAnswerXml] combineWith
      SPARQLSolutionWriterSelector[Jena, SparqlAnswerXml]

}
