package org.w3.banana.bigdata.io

import java.io.{ByteArrayInputStream, InputStream, Reader}

import org.openrdf.query.resultio.{BooleanQueryResultFormat, QueryResultIO, QueryResultParseException, TupleQueryResultFormat}
import org.w3.banana.bigdata.{BindingsAccumulator, Bigdata}
import org.w3.banana.io.{SparqlAnswerJson, SparqlAnswerXml, SparqlQueryResultsReader}

import scala.util.{Either, Left, Right, Try}


abstract class BigdataQueryResultsReader[S] extends SparqlQueryResultsReader[Bigdata, S] {

  def tupleFormat: TupleQueryResultFormat

  def booleanFormat: BooleanQueryResultFormat

  def read(in: InputStream, base: String) = {
    val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
    parse(bytes)
  }

  /** We first assume it is a tuple query, parse it, then parse it as a
    * boolean query
    */
  def parse(bytes: Array[Byte]): Try[Either[Bigdata#Solutions, Boolean]] = Try {
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


object BigdataQueryResultsReader {

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Bigdata, SparqlAnswerJson] =
    new BigdataQueryResultsReader[SparqlAnswerJson] {
      val tupleFormat = TupleQueryResultFormat.JSON
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+json")
    }

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Bigdata, SparqlAnswerXml] =
    new BigdataQueryResultsReader[SparqlAnswerXml] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+xml")
    }

}
