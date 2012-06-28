package org.w3.banana.jena

import java.io.{Writer, OutputStream}
import org.w3.banana._
import scalaz.Validation

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlSolutionsWriter  {

  def apply[SyntaxType](implicit jenaSparqlSyntax: SparqlAnswerOut[SyntaxType],
                    syntaxTp: Syntax[SyntaxType])
  :  SparqlSolutionsWriter[JenaSPARQL,SyntaxType] =
    new SparqlSolutionsWriter[JenaSPARQL, SyntaxType] {

      def write(answers: JenaSPARQL#Solutions, os: OutputStream, base: String ) =
        WrappedThrowable.fromTryCatch {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def write(input: JenaSPARQL#Solutions, writer: Writer, base: String ) = null

      def syntax[S >: SyntaxType] = syntaxTp
    }

  implicit val Json: SparqlSolutionsWriter[JenaSPARQL, SparqlAnswerJson] =
    SparqlSolutionsWriter[SparqlAnswerJson]

  implicit val XML: SparqlSolutionsWriter[JenaSPARQL, SparqlAnswerXML]  =
    SparqlSolutionsWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[JenaSPARQL#Solutions] =
    RDFWriterSelector[JenaSPARQL#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[JenaSPARQL#Solutions, SparqlAnswerJson]

}
