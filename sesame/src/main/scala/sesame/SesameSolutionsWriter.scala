package org.w3.banana.sesame

import org.w3.banana._
import java.io.{ ByteArrayOutputStream, Writer => jWriter }
import scalax.io._
import scala.util._

/**
 * Creates a blocking Sparql writer for the given syntax
 */
object SesameSolutionsWriter {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerOutput[T], _syntax: Syntax[T]) =
    new SparqlSolutionsWriter[Sesame, T] {

      val syntax = _syntax

      def write[R <: jWriter](answers: Sesame#Solutions, wcr: WriteCharsResource[R], base: String) =
        Try {
          val baos = new ByteArrayOutputStream()
          val sWriter = sesameSparqlSyntax.writer(baos)
          // sWriter.startQueryResult(answers.getBindingNames)
          sWriter.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
          answers foreach { answer => sWriter.handleSolution(answer) }
          sWriter.endQueryResult()
          wcr.write(baos.toString("UTF-8"))
        }

    }

  implicit val solutionsWriterJson = SesameSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml = SesameSolutionsWriter[SparqlAnswerXml]

  implicit val writerSelector: SparqlSolutionsWriterSelector[Sesame] =
    SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Sesame, SparqlAnswerXml]

}
