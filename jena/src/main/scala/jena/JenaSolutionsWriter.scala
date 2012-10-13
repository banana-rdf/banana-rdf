package org.w3.banana.jena

import java.io.{ ByteArrayOutputStream, Writer => jWriter }
import scalax.io._
import org.w3.banana._
import scala.util._

/**
 * Creates a Sparql writer for the given syntax
 */
object JenaSolutionsWriter {

  def apply[T](implicit jenaSparqlSyntax: JenaAnswerOutput[T], _syntax: Syntax[T]): SparqlSolutionsWriter[Jena, T] =
    new SparqlSolutionsWriter[Jena, T] {

      val syntax = _syntax

      def write[R <: jWriter](answers: Jena#Solutions, wcr: WriteCharsResource[R], base: String) =
        Try {
          // Jena's OutputFormater has no method operating over a Writer
          // so we need to a temporary String
          val baos = new ByteArrayOutputStream()
          jenaSparqlSyntax.formatter.format(baos, answers)
          wcr.write(baos.toString("UTF-8"))
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
