package org.w3.banana.sesame

import org.w3.banana._
import java.io.{ Writer, OutputStream }

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter {

  def apply[T](implicit sesameSparqlSyntax: SparqlAnswerOut[T], _syntax: Syntax[T]) =
    new SPARQLSolutionsWriter[Sesame, T] {

      val syntax = _syntax

      def write(answers: Sesame#Solutions, os: OutputStream, base: String = "") = {
        WrappedThrowable.fromTryCatch {
          val w = sesameSparqlSyntax.writer(os)
          // w.startQueryResult(answers.getBindingNames)
          w.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
          answers foreach { answer => w.handleSolution(answer) }
          os.flush()
          w.endQueryResult()
        }
      }

      def write(input: Sesame#Solutions, writer: Writer, base: String) = null

    }

  implicit val Json = SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML = SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val writerSelector: SPARQLSolutionsWriterSelector[Sesame] =
    SPARQLSolutionWriterSelector[Sesame, SparqlAnswerXML] combineWith
      SPARQLSolutionWriterSelector[Sesame, SparqlAnswerXML]

}
