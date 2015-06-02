package org.w3.banana.bigdata.io


import java.io._
import org.w3.banana._
import org.w3.banana.bigdata.Bigdata
import org.w3.banana.io._
import scala.util._
import org.openrdf.query.resultio.TupleQueryResultWriter
private abstract class BigdataSolutionsWriter[S] extends SparqlSolutionsWriter[Bigdata, S] {

  def writer(outputStream: OutputStream): TupleQueryResultWriter

  def write(answers: Bigdata#Solutions, os: OutputStream, base: String) = Try {
    val sWriter = writer(os)
    // sWriter.startQueryResult(answers.getBindingNames)
    sWriter.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
    answers.foreach { answer => sWriter.handleSolution(answer) }
    sWriter.endQueryResult()
  }

  def asString(answers: Bigdata#Solutions, base: String) = Try {
    // TODO this goes through the lossy outputstream in order to then
    // recode a string, even though it does not really know what the
    // required encoding is
    val out = new ByteArrayOutputStream()
    write(answers, out, base)
    new String(out.toByteArray, "UTF-8")
  }

}


object BigdataSolutionsWriter {

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Bigdata, SparqlAnswerJson] = new BigdataSolutionsWriter[SparqlAnswerJson] {

    import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter

    def writer(outputStream: OutputStream): TupleQueryResultWriter = new SPARQLResultsJSONWriter(outputStream)

  }

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Bigdata, SparqlAnswerXml] = new BigdataSolutionsWriter[SparqlAnswerXml] {

    import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter

    def writer(outputStream: OutputStream): TupleQueryResultWriter = new SPARQLResultsXMLWriter(outputStream)

  }

}
