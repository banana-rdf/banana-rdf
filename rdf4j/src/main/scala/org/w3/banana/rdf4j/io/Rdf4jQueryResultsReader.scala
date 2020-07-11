package org.w3.banana.rdf4j.io

import java.io._

import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.query.resultio._
import org.w3.banana.io.SparqlQueryResultsReader
import org.w3.banana.io._
import org.w3.banana.rdf4j.{BindingsAccumulator, Rdf4j}

import scala.util._

private abstract class Rdf4jQueryResultsReader[S] extends SparqlQueryResultsReader[Rdf4j, S] {

  def tupleFormat: TupleQueryResultFormat

  def booleanFormat: BooleanQueryResultFormat

  def read(in: InputStream, base: String) = {
    val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
    parse(bytes)
  }

  /** We first assume it is a boolean query, parse it, then parse it as a
    * tuple query
    */
  def parse(bytes: Array[Byte]): Try[Either[Rdf4j#Solutions, Boolean]] = Try {
    try {
      val parsed = QueryResultIO.parseBoolean(new ByteArrayInputStream(bytes), booleanFormat)
      Right(parsed)
    } catch {
      case e: QueryResultParseException =>
        val enumerator = new BindingsAccumulator()
        QueryResultIO.parseTuple(
          new ByteArrayInputStream(bytes),
          tupleFormat,
          enumerator,
          SimpleValueFactory.getInstance())
        Left(enumerator.bindings())
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
      val booleanFormat = BooleanQueryResultFormat.JSON
    }

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Rdf4j, SparqlAnswerXml] =
    new Rdf4jQueryResultsReader[SparqlAnswerXml] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = BooleanQueryResultFormat.SPARQL
    }

}
