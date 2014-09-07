package org.w3.banana.jena

import java.io._

import org.w3.banana._

import scala.util._

/**
 * Creates a Sparql writer for the given syntax
 */
object JenaSolutionsWriter {

  def apply[T](implicit jenaSparqlSyntax: JenaAnswerOutput[T], _syntax: Syntax[T]): SparqlSolutionsWriter[Jena, T] =
    new SparqlSolutionsWriter[Jena, T] {

      val syntax = _syntax

      def write(answers: Jena#Solutions, os: OutputStream, base: String) =
        Try {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def asString(answers: Jena#Solutions, base: String): Try[String] = Try {
        val result = new ByteArrayOutputStream()
        jenaSparqlSyntax.formatter.format(result, answers)
        answers.toString
      }
    }

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter[SparqlAnswerXml]

  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] =
    SparqlSolutionWriterSelector[Jena, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Jena, SparqlAnswerXml]

}
