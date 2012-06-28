package org.w3.banana.sesame

import org.w3.banana._
import jena.RDFWriterSelector
import java.io.{Writer, OutputStream}


/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter {

  def apply[SyntaxType](implicit sesameSparqlSyntax: SparqlAnswerOut[SyntaxType],
                         syntax: Syntax[SyntaxType]) =
    new SparqlSolutionsWriter[SesameSPARQL, SyntaxType] {

      def write(answers: SesameSPARQL#Solutions, os: OutputStream, base: String = "") = {
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

      def write(input: SesameSPARQL#Solutions, writer: Writer, base: String) = null

      def syntax[S >: SyntaxType] = syntax
    }

  implicit val Json = SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML =  SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[SesameSPARQL#Solutions] =
    RDFWriterSelector[SesameSPARQL#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[SesameSPARQL#Solutions, SparqlAnswerJson]

}
