package org.w3.banana.sesame

import org.w3.banana.{SparqlAnswerXML, SparqlAnswerJson, WrappedThrowable, BlockingSparqlAnswerWriter}
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter
import java.io.OutputStream
import org.openrdf.query.TupleQueryResult
import org.openrdf.query.resultio.TupleQueryResultWriter
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter


/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SesameSparqlBlockingWriter {

  def apply[SyntaxType](implicit sesameSparqlSyntax: SesameSparqlOutputSyntax[SyntaxType]) =
    new BlockingSparqlAnswerWriter[SesameSPARQL, SyntaxType] {

      def write(answers: SesameSPARQL#Solutions, os: OutputStream) = {
        WrappedThrowable.fromTryCatch {
          val w = sesameSparqlSyntax.writer(os)
          while(answers.hasNext) {
            w.handleSolution(answers.next())
          }
        }
      }

      def write(answer: Boolean, os: OutputStream) = null //todo
    }

  implicit val SparqlAnswerJson = SesameSparqlBlockingWriter[SparqlAnswerJson]

  implicit val SparqlAnswerXML = SesameSparqlBlockingWriter[SparqlAnswerXML]

}
