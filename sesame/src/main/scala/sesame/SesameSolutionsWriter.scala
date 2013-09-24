package org.w3.banana.sesame

import org.w3.banana._
import java.io._
import scala.util._

/**
 * Creates a blocking Sparql writer for the given syntax
 */
object SesameSolutionsWriter {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerOutput[T], _syntax: Syntax[T]) =
    new SparqlSolutionsWriter[Sesame, T] {

      val syntax = _syntax

      def write(answers: Sesame#Solutions, os: OutputStream, base: String) = Try {
        val sWriter = sesameSparqlSyntax.writer(os)
        // sWriter.startQueryResult(answers.getBindingNames)
        sWriter.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
        answers foreach { answer => sWriter.handleSolution(answer) }
        sWriter.endQueryResult()
      }

    }

  implicit val solutionsWriterJson = SesameSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml = SesameSolutionsWriter[SparqlAnswerXml]

  implicit val writerSelector: SparqlSolutionsWriterSelector[Sesame] =
    SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml]

}
