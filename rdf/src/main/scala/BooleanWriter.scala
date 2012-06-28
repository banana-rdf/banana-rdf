package org.w3.banana

import java.io.{OutputStream, Writer}
import jena.RDFWriterSelector


trait BooleanWriter[+SyntaxType] extends BlockingWriter[Boolean, SyntaxType]

object BooleanWriter {


  /**
   * <a href="http://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query Results JSON Format</a>
   */
  implicit val Json = new SimpleBooleanWriter[SparqlAnswerJson] {
    def format(result: Boolean) =
      """{
        |  "head": {}
        |  "boolean" : %s
        |}
        | """.stripMargin.format(result)

    def syntax[S >: SparqlAnswerJson] = Syntax.SparqlAnswerJson
  }

  /**
   * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML Format</a>
   */
  implicit val XML = new SimpleBooleanWriter[SparqlAnswerXML] {
    def format(result: Boolean) =
      """<?xml version="1.0"?>
        |<sparql xmlns="http://www.w3.org/2005/sparql-results#">
        |  <head/>
        |  <boolean>%s</boolean>
        |</sparql> """.stripMargin.format(result)

    def syntax[S >: SparqlAnswerXML] = Syntax.SparqlAnswerXML
  }


  implicit val WriterSelector: RDFWriterSelector[Boolean] =
    RDFWriterSelector[Boolean, SparqlAnswerXML] combineWith
      RDFWriterSelector[Boolean, SparqlAnswerJson]

}

trait SimpleBooleanWriter[+SyntaxType] extends BooleanWriter[SyntaxType] {

  def format(result: Boolean): String

  def write(input: Boolean, os: OutputStream, base: String) = WrappedThrowable.fromTryCatch {
    os.write(format(input).getBytes("UTF-8")) //todo: the context for encodings is nowhere here, it needs to be passed
  }

  def write(input: Boolean, writer: Writer, base: String) = WrappedThrowable.fromTryCatch {
    writer.write(format(input))
  }

}