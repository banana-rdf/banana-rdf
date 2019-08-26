package org.w3.banana.rd4j.io

import java.io._

import org.eclipse.rdf4j.query.resultio.TupleQueryResultWriter
import org.w3.banana._
import org.w3.banana.io.{SparqlAnswerJson, SparqlAnswerXml}
import org.w3.banana.rd4j.Rdf4j

import scala.util._

private abstract class Rdf4jSolutionsWriter[S] extends SparqlSolutionsWriter[Rdf4j, S] {

  def writer(outputStream: OutputStream): TupleQueryResultWriter

  def write(answers: Rdf4j#Solutions, os: OutputStream, base: String) = Try {
    val sWriter = writer(os)
    // sWriter.startQueryResult(answers.getBindingNames)
    sWriter.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
    answers.foreach { answer => sWriter.handleSolution(answer) }
    sWriter.endQueryResult()
  }

  def asString(answers: Rdf4j#Solutions, base: String) = Try {
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
