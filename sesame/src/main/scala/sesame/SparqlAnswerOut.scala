package org.w3.banana.sesame

import java.io.OutputStream
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter
import org.openrdf.query.resultio.TupleQueryResultWriter
import org.w3.banana.{ SparqlAnswerXML, SparqlAnswerJson }

/**
 * Sparql Output Syntaxes for Sesame, dependent on a SyntaxType for type classes
 * @tparam SyntaxType  They type of Syntax the
 */
trait SparqlAnswerOut[SyntaxType] {
  def writer(outputStream: OutputStream): TupleQueryResultWriter
}

object SparqlAnswerOut {

  implicit val Json: SparqlAnswerOut[SparqlAnswerJson] =
    new SparqlAnswerOut[SparqlAnswerJson] {
      def writer(outputStream: OutputStream) = new SPARQLResultsJSONWriter(outputStream)
    }

  implicit val XML: SparqlAnswerOut[SparqlAnswerXML] =
    new SparqlAnswerOut[SparqlAnswerXML] {
      def writer(outputStream: OutputStream) = new SPARQLResultsXMLWriter(outputStream)
    }

}
