package org.w3.banana.sesame

import org.openrdf.query.resultio.{ BooleanQueryResultFormat, TupleQueryResultFormat }
import org.w3.banana._

/**
 * typeclass for serialising special
 * @tparam T
 */
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

