package org.w3.banana.jena

import com.hp.hpl.jena.sparql.resultset.{XMLOutput, JSONOutput, OutputFormatter}
import org.w3.banana.{SparqlAnswerXML, SparqlAnswerJson}


/**
 * typeclass for serialising special
 * @tparam T
 */
trait JenaSparqlOutputSyntax[T] {
  def formatter: OutputFormatter
}


object JenaSparqlOutputSyntax {

  implicit val SparqlAnswerJson: JenaSparqlOutputSyntax[SparqlAnswerJson] =
    new JenaSparqlOutputSyntax[SparqlAnswerJson] {
      def formatter = new JSONOutput()
    }

  implicit val SparqlAnswerXML: JenaSparqlOutputSyntax[SparqlAnswerXML] =
    new JenaSparqlOutputSyntax[SparqlAnswerXML] {
      def formatter = new XMLOutput()
    }


}

