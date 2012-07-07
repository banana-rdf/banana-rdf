package org.w3.banana.jena

import com.hp.hpl.jena.sparql.resultset.{ XMLOutput, JSONOutput, OutputFormatter }
import org.w3.banana._

/**
 * typeclass for serialising special
 * @tparam T
 */
trait SparqlAnswerOut[T] {
  def formatter: OutputFormatter
}

object SparqlAnswerOut {

  implicit val Json: SparqlAnswerOut[SparqlAnswerJson] =
    new SparqlAnswerOut[SparqlAnswerJson] {
      def formatter = new JSONOutput()
    }

  implicit val XML: SparqlAnswerOut[SparqlAnswerXML] =
    new SparqlAnswerOut[SparqlAnswerXML] {
      def formatter = new XMLOutput()
    }

}

