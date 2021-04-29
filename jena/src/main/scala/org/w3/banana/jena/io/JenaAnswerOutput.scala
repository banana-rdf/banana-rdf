package org.w3.banana.jena.io

import org.apache.jena.riot.resultset.ResultSetLang
import org.w3.banana.io._

/**
 * typeclass for serialising special
 */
trait JenaAnswerOutput[T] {
  def formatter: org.apache.jena.riot.Lang
}

object JenaAnswerOutput {

  implicit val Json: JenaAnswerOutput[SparqlAnswerJson] =
    new JenaAnswerOutput[SparqlAnswerJson] {
      def formatter =  ResultSetLang.SPARQLResultSetJSON
    }

  implicit val XML: JenaAnswerOutput[SparqlAnswerXml] =
    new JenaAnswerOutput[SparqlAnswerXml] {
      def formatter = ResultSetLang.SPARQLResultSetXML
    }

}

