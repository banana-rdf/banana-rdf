package org.w3.banana.sesame

import java.io._

import org.w3.banana._

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

      override def asString(answers: Sesame#Solutions, base: String) = Try {
        //todo: this is very ugly, as it goes through the lossy outputstream in order to then recode
        //todo: a string, even though it does not really know what the required encoding is
        val out = new ByteArrayOutputStream()
        write(answers, out, base)
        new String(out.toByteArray, "UTF-8")
      }
    }

  implicit val solutionsWriterJson = SesameSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml = SesameSolutionsWriter[SparqlAnswerXml]

  implicit val writerSelector: SparqlSolutionsWriterSelector[Sesame] =
    SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml]

}
