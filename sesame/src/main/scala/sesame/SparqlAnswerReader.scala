package org.w3.banana.sesame


import java.io.InputStream
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

      def read(in: InputStream) =  WrappedThrowable.fromTryCatch {
        QueryResultIO.parse(in,sesameSparqlSyntax.format)
      }
    }

  implicit val Json: SparqlQueryResultsReader[SesameSPARQL, SparqlAnswerJson] =
    SparqlAnswerReader[SparqlAnswerJson]

  implicit val forXML: SparqlQueryResultsReader[SesameSPARQL, SparqlAnswerXML] =
    SparqlAnswerReader[SparqlAnswerXML]

}
