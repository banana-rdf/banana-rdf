package org.w3.banana.plantain

import java.io.{ OutputStream, Writer => jWriter }

import org.openrdf.query.resultio.TupleQueryResultWriter
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter
import org.w3.banana._

import scala.util._

object PlantainSolutionsWriter {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerOutput[T], _syntax: Syntax[T]) =
    new SparqlSolutionsWriter[Plantain, T] {

      val syntax = _syntax

      def write(answers: Plantain#Solutions, os: OutputStream, base: String) = {
        import scala.collection.convert.wrapAsJava._
        val bindings = answers.bindings
        Try {
          val sWriter = sesameSparqlSyntax.writer(os)
          sWriter.startQueryResult(bindings)
          answers.iterator foreach { answer => sWriter.handleSolution(answer) }
          sWriter.endQueryResult()
        }
      }

      override def asString(obj: Plantain#Solutions, base: String) = ???
    }

  implicit val solutionsWriterJson = PlantainSolutionsWriter[SparqlAnswerJson]

  implicit val solutionsWriterXml = PlantainSolutionsWriter[SparqlAnswerXml]

  implicit val writerSelector: SparqlSolutionsWriterSelector[Plantain] =
    SparqlSolutionWriterSelector[Plantain, SparqlAnswerXml] combineWith
      SparqlSolutionWriterSelector[Plantain, SparqlAnswerXml]

}

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
