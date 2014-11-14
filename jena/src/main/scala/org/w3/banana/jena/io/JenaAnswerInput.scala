package org.w3.banana.jena.io

import java.io.InputStream
import org.w3.banana.io._

import com.hp.hpl.jena.sparql.resultset.{ JSONInput, SPARQLResult, XMLInput }

/**
 * typeclass for serialising special
 */
trait JenaAnswerInput[T] {
  def parse(in: InputStream): SPARQLResult
}

object JenaAnswerInput {

  implicit val Json: JenaAnswerInput[SparqlAnswerJson] =
    new JenaAnswerInput[SparqlAnswerJson] {
      def parse(in: InputStream) = JSONInput.make(in)
    }

  implicit val XML: JenaAnswerInput[SparqlAnswerXml] =
    new JenaAnswerInput[SparqlAnswerXml] {
      def parse(in: InputStream) = XMLInput.make(in)
    }

}

