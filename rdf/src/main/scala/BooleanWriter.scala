package org.w3.banana

import scalax.io._
import scala.util._

trait BooleanWriter[T] extends Writer[Boolean, T] {

  def format(bool: Boolean): String

  def write[R](bool: Boolean, wcr: WriteCharsResource[R], base: String): Try[Unit] = Try {
    wcr.write(format(bool))
  }

}

object BooleanWriter {

  /**
   * <a href="http://www.w3.org/TR/sparql11-results-json/">Sparql 1.1 Query Results JSON Format</a>
   */
  implicit val Json = new BooleanWriter[SparqlAnswerJson] {

    val syntax = Syntax[SparqlAnswerJson]

    def format(bool: Boolean): String =
      """{
        |  "head": {},
        |  "boolean" : %s
        |}
        | """.stripMargin.format(bool)

  }

  /**
   * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">Sparql Query Results XML Format</a>
   */
  implicit val booleanWriterXml = new BooleanWriter[SparqlAnswerXml] {

    val syntax = Syntax[SparqlAnswerXml]

    def format(bool: Boolean): String =
      """<?xml version="1.0"?>
        |<sparql xmlns="http://www.w3.org/2005/sparql-results#">
        |  <head/>
        |  <boolean>%s</boolean>
        |</sparql> """.stripMargin.format(bool) // "

  }

  implicit val selector: WriterSelector[Boolean] =
    WriterSelector[Boolean, SparqlAnswerXml] combineWith WriterSelector[Boolean, SparqlAnswerJson]

}
