package org.w3.banana.plantain

import java.io._

import org.openrdf.query.resultio.{ QueryResultIO, QueryResultParseException }
import org.w3.banana._
import org.w3.banana.plantain.PlantainUtil._

import scala.util._

object PlantainQueryResultsReader {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerInput[T]): SparqlQueryResultsReader[Plantain, T] =
    new SparqlQueryResultsReader[Plantain, T] {

      def read(in: InputStream, base: String) = {
        val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
        parse(bytes)
      }

      def parse(bytes: Array[Byte]): Try[Either[Plantain#Solutions, Boolean]] = Try {
        try {
          val parsed = QueryResultIO.parse(new ByteArrayInputStream(bytes),
            sesameSparqlSyntax.booleanFormat)
          Right(parsed)
        } catch {
          case e: QueryResultParseException =>
            val tupleQueryResult =
              QueryResultIO.parse(new ByteArrayInputStream(bytes), sesameSparqlSyntax.tupleFormat)
            import scala.collection.convert.wrapAsScala._
            Left(BoundSolutions(tupleQueryResult.toIterator, tupleQueryResult.getBindingNames.toList))
        }
      }

      def read(reader: Reader, base: String) = {
        val queri = Iterator.continually(reader.read).takeWhile((-1).!=).map(_.toChar).toArray
        //it is really horrible to have to turn a nice char array into bytes for parsing!
        parse(new String(queri).getBytes("UTF-8"))
      }

    }

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Plantain, SparqlAnswerJson] =
    PlantainQueryResultsReader[SparqlAnswerJson]

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Plantain, SparqlAnswerXml] =
    PlantainQueryResultsReader[SparqlAnswerXml]

}

/* copied from banana-sesame */

import org.openrdf.query.resultio.{ BooleanQueryResultFormat, TupleQueryResultFormat }

trait SesameAnswerInput[T] {
  def tupleFormat: TupleQueryResultFormat
  def booleanFormat: BooleanQueryResultFormat
}

object SesameAnswerInput {

  implicit val Json: SesameAnswerInput[SparqlAnswerJson] =
    new SesameAnswerInput[SparqlAnswerJson] {
      val tupleFormat = TupleQueryResultFormat.JSON
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+json")
    }

  implicit val XML: SesameAnswerInput[SparqlAnswerXml] =
    new SesameAnswerInput[SparqlAnswerXml] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+xml")
    }

}
