package org.w3.banana.jena.io

import java.io.{ InputStream, Reader }

import org.w3.banana._
import org.w3.banana.io.SparqlQueryResultsReader
import org.w3.banana.jena.Jena
import org.w3.banana.io._
import org.apache.jena.sparql.resultset.{ JSONInput, SPARQLResult, XMLInput }
import scala.util._

abstract private class JenaQueryResultsReader[S] extends SparqlQueryResultsReader[Jena, S] {

  def parse(in: InputStream): SPARQLResult

  def read(in: InputStream, base: String = ""): Try[Either[Jena#Solutions, Boolean]] = Try {
    val resultSet = parse(in)
    if (resultSet.isBoolean) {
      Right(resultSet.getBooleanResult)
    } else if (resultSet.isResultSet) {
      Left(resultSet.getResultSet)
    } else {
      throw new WrongExpectation("was expecting either a boolean or result set answer. received a model? " + resultSet.isModel)
    }
  }

  def read(reader: Reader, base: String) = ???

}

object JenaQueryResultsReader {

  type Answer = Either[Jena#Solutions, Boolean]

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    new JenaQueryResultsReader[SparqlAnswerJson] {
      def parse(in: InputStream) = JSONInput.make(in)
    }

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    new JenaQueryResultsReader[SparqlAnswerXml] {
      def parse(in: InputStream) = XMLInput.make(in)
    }

}
