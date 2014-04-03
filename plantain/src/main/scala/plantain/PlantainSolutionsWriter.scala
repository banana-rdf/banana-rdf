package org.w3.banana.plantain

import org.w3.banana._
import java.io.{ ByteArrayOutputStream, Writer => jWriter }
import scalax.io._
import scala.util._

object PlantainSolutionsWriter {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerOutput[T], _syntax: Syntax[T]) =
    new SparqlSolutionsWriter[Plantain, T] {

      val syntax = _syntax

      def write[R <: jWriter](answers: Plantain#Solutions, wcr: WriteCharsResource[R], base: String) = {
        import collection.convert.wrapAsJava._
        val bindings = answers.bindings
        Try {
          val baos = new ByteArrayOutputStream()
          val sWriter = sesameSparqlSyntax.writer(baos)
          sWriter.startQueryResult(bindings)
          answers.iterator foreach { answer => sWriter.handleSolution(answer) }
          sWriter.endQueryResult()
          wcr.write(baos.toString("UTF-8"))
        }
      }
    }

  implicit val solutionsWriterJson = PlantainSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml = PlantainSolutionsWriter[SparqlAnswerXml]

  implicit val writerSelector: SparqlSolutionsWriterSelector[Plantain] =
    SparqlSolutionWriterSelector[Plantain, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Plantain, SparqlAnswerXml]

}

/* copied from banana-sesame */

import java.io.OutputStream
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter
import org.openrdf.query.resultio.TupleQueryResultWriter

trait SesameAnswerOutput[SyntaxType] {
  def writer(outputStream: OutputStream): TupleQueryResultWriter
}

object SesameAnswerOutput {

  implicit val Json: SesameAnswerOutput[SparqlAnswerJson] =
    new SesameAnswerOutput[SparqlAnswerJson] {
      def writer(outputStream: OutputStream) = new SPARQLResultsJSONWriter(outputStream)
    }

  implicit val XML: SesameAnswerOutput[SparqlAnswerXml] =
    new SesameAnswerOutput[SparqlAnswerXml] {
      def writer(outputStream: OutputStream) = new SPARQLResultsXMLWriter(outputStream)
    }

}
