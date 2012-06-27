package org.w3.banana.sesame

import org.w3.banana._
import jena.RDFWriterSelector
import java.io.{Writer, OutputStream}


/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerWriter {

  def apply[SyntaxType](implicit sesameSparqlSyntax: SparqlAnswerOut[SyntaxType]) =
    new BlockingSparqlAnswerWriter[SesameSPARQL, SyntaxType] {

      def write(answers: SesameSPARQL#Solutions, os: OutputStream) = {
        WrappedThrowable.fromTryCatch {
          val w = sesameSparqlSyntax.writer(os)
          w.startQueryResult(answers.getBindingNames)
          while(answers.hasNext) {
            w.handleSolution(answers.next())
          }
          os.flush()
          w.endQueryResult()
        }
      }

      def write(answer: Boolean, os: OutputStream) = null //todo
    }

  implicit val Json = SparqlAnswerWriter[SparqlAnswerJson]

  implicit val XML =  SparqlAnswerWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[SesameSPARQL#Solutions] =
    RDFWriterSelector[SesameSPARQL#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[SesameSPARQL#Solutions, SparqlAnswerJson]

}
