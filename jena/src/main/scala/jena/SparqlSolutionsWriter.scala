package org.w3.banana.jena

import java.io.{ Writer, OutputStream }
import org.w3.banana._
import scalaz.Validation

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter {

  def apply[SyntaxType](implicit jenaSparqlSyntax: SparqlAnswerOut[SyntaxType],
    syntaxTp: Syntax[SyntaxType]): SparqlSolutionsWriter[Jena, SyntaxType] =
    new SparqlSolutionsWriter[Jena, SyntaxType] {

      def write(answers: Jena#Solutions, os: OutputStream, base: String) =
        WrappedThrowable.fromTryCatch {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def write(input: Jena#Solutions, writer: Writer, base: String) = null

      def syntax[S >: SyntaxType] = syntaxTp
    }

  implicit val Json: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML: SparqlSolutionsWriter[Jena, SparqlAnswerXML] =
    SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[Jena#Solutions] =
    RDFWriterSelector[Jena#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[Jena#Solutions, SparqlAnswerXML]

}
