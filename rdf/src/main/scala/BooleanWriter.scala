package org.w3.banana

import java.io.{ Writer => jWriter, _ }

trait BooleanWriter[T] extends Writer[Boolean, T] {

  def format(result: Boolean): String

  def write(input: Boolean, os: OutputStream, base: String) = WrappedThrowable.fromTryCatch {
    os.write(format(input).getBytes("UTF-8")) //todo: the context for encodings is nowhere here, it needs to be passed
  }

  def write(input: Boolean, writer: jWriter, base: String) = WrappedThrowable.fromTryCatch {
    writer.write(format(input))
  }

}

object BooleanWriter {

  /**
   * <a href="http://www.w3.org/TR/sparql11-results-json/">Sparql 1.1 Query Results JSON Format</a>
   */
  implicit val Json = new BooleanWriter[SparqlAnswerJson] {

    val syntax = Syntax[SparqlAnswerJson]

    def format(result: Boolean) =
      """{
        |  "head": {},
        |  "boolean" : %s
        |}
        | """.stripMargin.format(result)

  }

  /**
   * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">Sparql Query Results XML Format</a>
   */
  implicit val booleanWriterXml = new BooleanWriter[SparqlAnswerXml] {

    val syntax = Syntax[SparqlAnswerXml]

    def format(result: Boolean) =
      """<?xml version="1.0"?>
        |<sparql xmlns="http://www.w3.org/2005/sparql-results#">
        |  <head/>
        |  <boolean>%s</boolean>
        |</sparql> """.stripMargin.format(result)

  }

  implicit val selector: WriterSelector[Boolean] =
    WriterSelector[Boolean, SparqlAnswerXml] combineWith WriterSelector[Boolean, SparqlAnswerJson]

}
