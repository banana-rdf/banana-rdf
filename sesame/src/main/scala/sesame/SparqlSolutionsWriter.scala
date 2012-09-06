package org.w3.banana.sesame

import org.w3.banana._
import jena.RDFWriterSelector
import java.io.{ Writer, OutputStream }

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter {

  def apply[SyntaxType](implicit sesameSparqlSyntax: SparqlAnswerOut[SyntaxType],
    syntaxTp: Syntax[SyntaxType]) =
    new SparqlSolutionsWriter[Sesame, SyntaxType] {

      def write(answers: Sesame#Solutions, os: OutputStream, base: String = "") = {
        WrappedThrowable.fromTryCatch {
          val w = sesameSparqlSyntax.writer(os)
          // w.startQueryResult(answers.getBindingNames)
          w.startQueryResult(new java.util.ArrayList()) // <- yeah, probably wrong...
          answers foreach { answer => w.handleSolution(answer) }
          os.flush()
          w.endQueryResult()
        }
      }

      def write(input: Sesame#Solutions, writer: Writer, base: String) = null

      def syntax[S >: SyntaxType] = syntaxTp
    }

  implicit val Json = SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML = SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[Sesame#Solutions] =
    RDFWriterSelector[Sesame#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[Sesame#Solutions, SparqlAnswerXML]

}
