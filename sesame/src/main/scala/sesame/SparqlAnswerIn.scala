package org.w3.banana.sesame

import java.io.InputStream
import org.w3.banana.{SparqlAnswerXML, SparqlAnswerJson}
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParserFactory
import org.openrdf.query.resultio.{BooleanQueryResultFormat, TupleQueryResultFormat}


/**
 * typeclass for serialising special
 * @tparam T
 */
trait SparqlAnswerIn[T] {
  def tupleFormat: TupleQueryResultFormat
  def booleanFormat: BooleanQueryResultFormat
}


object SparqlAnswerIn {

  implicit val Json: SparqlAnswerIn[SparqlAnswerJson] =
    new SparqlAnswerIn[SparqlAnswerJson] {
      val tupleFormat = TupleQueryResultFormat.JSON
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+json")
    }

  implicit val XML: SparqlAnswerIn[SparqlAnswerXML] =
    new SparqlAnswerIn[SparqlAnswerXML] {
      val tupleFormat = TupleQueryResultFormat.SPARQL
      val booleanFormat = BooleanQueryResultFormat.forMIMEType("application/sparql-results+xml")
    }


}

