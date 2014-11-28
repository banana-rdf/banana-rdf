package org.w3.banana.sesame.io

import java.io._

import org.openrdf.query.resultio._
import org.w3.banana.io.SparqlQueryResultsReader
import org.w3.banana.sesame.{ BindingsAccumulator, Sesame }
import org.w3.banana.io._

import scala.util._

private abstract class SesameQueryResultsReader[S] extends SparqlQueryResultsReader[Sesame, S] {

  def tupleFormat: TupleQueryResultFormat

  def booleanFormat: BooleanQueryResultFormat

  def read(in: InputStream, base: String) = {
    val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
    parse(bytes)
  }

  /** We first assume it is a tuple query, parse it, then parse it as a
    * boolean query
    */
  def parse(bytes: Array[Byte]): Try[Either[Sesame#Solutions, Boolean]] = Try {
    try {
      val parsed = QueryResultIO.parse(new ByteArrayInputStream(bytes), booleanFormat)
      Right(parsed)
    } catch {
      case e: QueryResultParseException =>
        val enumerator = new BindingsAccumulator()
        QueryResultIO.parse(
          new ByteArrayInputStream(bytes),
          tupleFormat,
          enumerator,
          org.openrdf.model.impl.ValueFactoryImpl.getInstance())
        Left(enumerator.bindings())
    }
  }

  def read(reader: Reader, base: String) = {
    val queri = Iterator.continually(reader.read).takeWhile((-1).!=).map(_.toChar).toArray
    parse(new String(queri).getBytes("UTF-8"))
  }

}

object SesameQueryResultsReader {

  val queryResultsReaderJson: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    new SesameQueryResultsReader[SparqlAnswerJson] {
      val tupleFormat = TupleQueryResultFormat.JSON
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+json")
    }

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    new SesameQueryResultsReader[SparqlAnswerXml] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+xml")
    }

}
