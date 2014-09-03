package org.w3.banana.sesame

import java.io.OutputStream

import org.openrdf.query.resultio.TupleQueryResultWriter
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter
import org.w3.banana._

/**
 * Sparql Output Syntaxes for Sesame, dependent on a SyntaxType for type classes
 * @tparam SyntaxType  They type of Syntax the
 */
trait SesameAnswerOutput[SyntaxType] {
  def writer(outputStream: OutputStream): TupleQueryResultWriter
}

object SesameAnswerOutput {

  implicit val Json: SesameAnswerOutput[SparqlAnswerJson] =
    new SesameAnswerOutput[SparqlAnswerJson] {
      def writer(outputStream: OutputStream) = new SPARQLResultsJSONWriter(outputStream)
    }

  implicit val XML: SesameAnswerOutput[SparqlAnswerXml] =
    new SesameAnswerOutput[SparqlAnswerXml] {
      def writer(outputStream: OutputStream) = new SPARQLResultsXMLWriter(outputStream)
    }

}
