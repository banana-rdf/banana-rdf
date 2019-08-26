package org.w3.banana.rd4j.io

import java.io._

import org.eclipse.rdf4j.query.QueryResultHandlerException
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector
import org.eclipse.rdf4j.query.resultio.{QueryResultFormat, QueryResultIO, TupleQueryResultFormat}
import org.w3.banana.io.{SparqlAnswerJson, SparqlAnswerXml, SparqlQueryResultsReader}
import org.w3.banana.rd4j.Rdf4j

import scala.collection.JavaConverters._
import scala.util._

private abstract class Rdf4jQueryResultsReader[S] extends SparqlQueryResultsReader[Rdf4j, S] {

  def tupleFormat: TupleQueryResultFormat

  def booleanFormat: QueryResultFormat

  def read(in: InputStream, base: String) = {
    val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
    parse(bytes)
  }

  def parse(bytes: Array[Byte]): Try[Either[Rdf4j#Solutions, Boolean]] = Try {
    val parser = QueryResultIO.createBooleanParser(booleanFormat)
    val parsed = new QueryResultCollector
    parser.setQueryResultHandler(new QueryResultCollector)
    parser.parseQueryResult(new ByteArrayInputStream(bytes))
    try {
      Right(parsed.getBoolean)
    } catch {
      case e: QueryResultHandlerException =>
        Left(parsed.getBindingSets.asScala.toStream)
    }
  }

  def read(reader: Reader, base: String) = {
    val queri = Iterator.continually(reader.read).takeWhile((-1).!=).map(_.toChar).toArray
    parse(new String(queri).getBytes("UTF-8"))
  }

}

object Rdf4jQueryResultsReader {

  val queryResultsReaderJson: SparqlQueryResultsReader[Rdf4j, SparqlAnswerJson] =
    new Rdf4jQueryResultsReader[SparqlAnswerJson] {
      val tupleFormat = TupleQueryResultFormat.JSON
      val booleanFormat = QueryResultIO.getBooleanParserFormatForMIMEType("application/sparql-results+json").get()
    }

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Rdf4j, SparqlAnswerXml] =
    new Rdf4jQueryResultsReader[SparqlAnswerXml] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = QueryResultIO.getBooleanParserFormatForMIMEType("application/sparql-results+xml").get()
    }

}

