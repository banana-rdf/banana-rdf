package org.w3.banana.jena

import com.hp.hpl.jena.sparql.resultset.{ JSONOutput, XMLOutput }
import java.io.OutputStream
import org.w3.banana._
import jena._
import scalaz.Either3

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerWriter {

  def apply[Syntax](implicit jenaSparqlSyntax: SparqlAnswerOut[Syntax]): BlockingSparqlAnswerWriter[JenaSPARQL, Syntax] =
    new BlockingSparqlAnswerWriter[JenaSPARQL, Syntax] {

      def write(answers: JenaSPARQL#Solutions, os: OutputStream) = WrappedThrowable.fromTryCatch {
        jenaSparqlSyntax.formatter.format(os, answers)
      }
      def write(answer: Boolean, os: OutputStream) = null //todo
    }

  implicit val Json: BlockingSparqlAnswerWriter[JenaSPARQL, SparqlAnswerJson] =
    SparqlAnswerWriter[SparqlAnswerJson]

  implicit val XML: BlockingSparqlAnswerWriter[JenaSPARQL, SparqlAnswerXML] =
    SparqlAnswerWriter[SparqlAnswerXML]

}
