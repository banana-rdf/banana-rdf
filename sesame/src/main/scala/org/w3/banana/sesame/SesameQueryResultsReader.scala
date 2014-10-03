package org.w3.banana.sesame

import java.io._

import org.openrdf.query.resultio.{ QueryResultIO, QueryResultParseException }
import org.w3.banana._

import scala.util._

/**
 *
 * typeclass for a blocking BlockingReader of Sparql Query Results
 * such as those defined
 * <ul>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-XMLres/">Sparql Query Results XML Format</a></li>
 *   <li><a href="http://www.w3.org/TR/rdf-sparql-json-res/">Sparql Query Results in JSON</a></li>
 * </ul>
 *
 * In Sesame the implementation is very ugly.
 * We first have to assume it is a tuple query, parse it, then parse it as a boolean query
 * See Issue <a href="http://www.openrdf.org/issues/browse/SES-1054">SES-1054</a>
 * We are waiting for a fix for this problem.
 *
 * If you can't wait for the fix, it would be better to write
 * a parser directly or a mapper from Jena's implementation. Parsing sparql or json queries can't be
 * that difficult.
 *
 */
object SesameQueryResultsReader {

  def apply[T](implicit sesameSparqlSyntax: SesameAnswerInput[T]): SparqlQueryResultsReader[Sesame, T] =
    new SparqlQueryResultsReader[Sesame, T] {

      def read(in: InputStream, base: String) = {
        val bytes: Array[Byte] = Iterator.continually(in.read).takeWhile((-1).!=).map(_.toByte).toArray
        parse(bytes)
      }

      def parse(bytes: Array[Byte]): Try[Either[Sesame#Solutions, Boolean]] = Try {
        try {
          val parsed = QueryResultIO.parse(new ByteArrayInputStream(bytes),
            sesameSparqlSyntax.booleanFormat)
          Right(parsed)
        } catch {
          case e: QueryResultParseException =>
            val enumerator = new BindingsAccumulator()
            QueryResultIO.parse(
              new ByteArrayInputStream(bytes),
              sesameSparqlSyntax.tupleFormat,
              enumerator,
              org.openrdf.model.impl.ValueFactoryImpl.getInstance())
            Left(enumerator.bindings())
        }
      }

      def read(reader: Reader, base: String) = {
        val queri = Iterator.continually(reader.read).takeWhile((-1).!=).map(_.toChar).toArray
        //it is really horrible to have to turn a nice char array into bytes for parsing!
        parse(new String(queri).getBytes("UTF-8"))
      }

    }

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader[SparqlAnswerJson]

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader[SparqlAnswerXml]

}
