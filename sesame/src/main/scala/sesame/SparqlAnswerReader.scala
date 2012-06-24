package org.w3.banana.sesame


import java.io.InputStream
import org.w3.banana._
import org.openrdf.query.resultio.QueryResultIO

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerReader {

  def apply[Syntax](implicit sesameSparqlSyntax: SparqlAnswerIn[Syntax]):
  BlockingSparqlAnswerReader[SesameSPARQL, Syntax] =
    new BlockingSparqlAnswerReader[SesameSPARQL, Syntax] {

      def read(in: InputStream) = WrappedThrowable.fromTryCatch {
        QueryResultIO.parse(in,sesameSparqlSyntax.format)
      }
    }

  implicit val Json: BlockingSparqlAnswerReader[SesameSPARQL, SparqlAnswerJson] =
    SparqlAnswerReader[SparqlAnswerJson]

  implicit val forXML: BlockingSparqlAnswerReader[SesameSPARQL, SparqlAnswerXML] =
    SparqlAnswerReader[SparqlAnswerXML]

}
