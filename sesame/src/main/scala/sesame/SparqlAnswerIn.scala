package org.w3.banana.sesame

import java.io.InputStream
import org.w3.banana.{ SparqlAnswerXML, SparqlAnswerJson }
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLParserFactory
import org.openrdf.query.resultio.TupleQueryResultFormat

/**
 * typeclass for serialising special
 * @tparam T
 */
trait SparqlAnswerIn[T] {
  def format: TupleQueryResultFormat
}

object SparqlAnswerIn {

  implicit val Json: SparqlAnswerIn[SparqlAnswerJson] =
    new SparqlAnswerIn[SparqlAnswerJson] {
      val format = TupleQueryResultFormat.JSON
    }

  implicit val XML: SparqlAnswerIn[SparqlAnswerXML] =
    new SparqlAnswerIn[SparqlAnswerXML] {
      val format = TupleQueryResultFormat.SPARQL
    }

}

