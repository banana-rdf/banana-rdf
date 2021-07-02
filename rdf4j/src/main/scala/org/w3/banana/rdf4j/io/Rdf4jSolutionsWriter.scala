package org.w3.banana.rdf4j.io

import java.io._

import org.w3.banana._
import org.w3.banana.io._

import scala.util._
import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter
import org.w3.banana.rdf4j.Rdf4j

private abstract class Rdf4jSolutionsWriter[S] extends SparqlSolutionsWriter[Rdf4j, S] {

  def writer(outputStream: OutputStream): TupleQueryResultWriter

  def write(answers: Rdf4j#Solutions, os: OutputStream, base: Option[String]) = Try {
    val sWriter = writer(os)
    // sWriter.startQueryResult(answers.getBindingNames)
    sWriter.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
    answers.foreach { answer => sWriter.handleSolution(answer) }
    sWriter.endQueryResult()
  }

  def asString(answers: Rdf4j#Solutions, base: Option[String]) = Try {
    // TODO this goes through the lossy outputstream in order to then
    // recode a string, even though it does not really know what the
    // required encoding is
    val out = new ByteArrayOutputStream()
    write(answers, out, base)
    new String(out.toByteArray, "UTF-8")
  }

}

object Rdf4jSolutionsWriter {

  val solutionsWriterJson: SparqlSolutionsWriter[Rdf4j, SparqlAnswerJson] = new Rdf4jSolutionsWriter[SparqlAnswerJson] {

    import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter

    def writer(outputStream: OutputStream): TupleQueryResultWriter = new SPARQLResultsJSONWriter(outputStream)

  }

  val solutionsWriterXml: SparqlSolutionsWriter[Rdf4j, SparqlAnswerXml] = new Rdf4jSolutionsWriter[SparqlAnswerXml] {

    import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter

    def writer(outputStream: OutputStream): TupleQueryResultWriter = new SPARQLResultsXMLWriter(outputStream)

  }

}
