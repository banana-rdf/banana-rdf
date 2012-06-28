package org.w3.banana.sesame


import java.io.{Reader, InputStream}
import org.w3.banana._
import org.openrdf.query.resultio.QueryResultIO
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerReader {

  def apply[Syntax](implicit sesameSparqlSyntax: SparqlAnswerIn[Syntax]):
  SparqlQueryResultsReader[SesameSPARQL, Syntax] =
    new SparqlQueryResultsReader[SesameSPARQL, Syntax] {

      def read(in: InputStream, base: String) = throw new NotImplementedException
//        WrappedThrowable.fromTryCatch {
//        val res = QueryResultIO.parse(in,sesameSparqlSyntax.format)
//      }

      def read(reader: Reader, base: String) = throw new NotImplementedException
    }

  implicit val Json: SparqlQueryResultsReader[SesameSPARQL, SparqlAnswerJson] =
    SparqlAnswerReader[SparqlAnswerJson]

  implicit val forXML: SparqlQueryResultsReader[SesameSPARQL, SparqlAnswerXML] =
    SparqlAnswerReader[SparqlAnswerXML]

}
