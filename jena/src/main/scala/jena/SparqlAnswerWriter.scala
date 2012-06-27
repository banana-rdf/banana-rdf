package org.w3.banana.jena

import java.io.{Writer, OutputStream}
import org.w3.banana._
import scalaz.Validation

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerWriter  {

  def apply[SyntaxType](implicit jenaSparqlSyntax: SparqlAnswerOut[SyntaxType],
                    syntax: Syntax[SyntaxType])
  :  BlockingSparqlAnswerWriter[JenaSPARQL,SyntaxType] =
    new BlockingSparqlAnswerWriter[JenaSPARQL, SyntaxType] {

      def write(answers: JenaSPARQL#Solutions, os: OutputStream, base: String ) =
        WrappedThrowable.fromTryCatch {
          jenaSparqlSyntax.formatter.format(os, answers)
        }

      def write(input: JenaSPARQL#Solutions, writer: Writer, base: String ) = null

      def syntax[S >: SyntaxType] = syntax
    }

  implicit val Json: BlockingSparqlAnswerWriter[JenaSPARQL, SparqlAnswerJson] =
    SparqlAnswerWriter[SparqlAnswerJson]

  implicit val XML: BlockingSparqlAnswerWriter[JenaSPARQL, SparqlAnswerXML]  =
    SparqlAnswerWriter[SparqlAnswerXML]

  implicit val WriterSelector: RDFWriterSelector[JenaSPARQL#Solutions] =
    RDFWriterSelector[JenaSPARQL#Solutions, SparqlAnswerXML] combineWith
      RDFWriterSelector[JenaSPARQL#Solutions, SparqlAnswerJson]

}
