package org.w3.banana.jena.io

import org.apache.jena.sparql.resultset.{ JSONOutput, OutputFormatter, XMLOutput }
import org.w3.banana.io._

/**
 * typeclass for serialising special
 */
trait JenaAnswerOutput[T] {
  def formatter: OutputFormatter
}

object JenaAnswerOutput {

  implicit val Json: JenaAnswerOutput[SparqlAnswerJson] =
    new JenaAnswerOutput[SparqlAnswerJson] {
      def formatter = new JSONOutput()
    }

  implicit val XML: JenaAnswerOutput[SparqlAnswerXml] =
    new JenaAnswerOutput[SparqlAnswerXml] {
      def formatter = new XMLOutput()
    }

}

