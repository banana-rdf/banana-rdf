import com.hp.hpl.jena.sparql.resultset.{JSONOutput, XMLOutput}
import java.io.OutputStream
import org.w3.banana._
import jena._
import scalaz.Either3

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object JenaSparqlBlockingWriter  {

  import JenaSparqlSyntax.SparqlAnswerJson
  import JenaSparqlSyntax.SparqlAnswerXML

  def apply[SyntaxType](implicit jenaSparqlSyntax: JenaSparqlOutputSyntax[SyntaxType]):
  BlockingSparqlAnswerWriter[JenaSPARQL,SyntaxType] =
    new BlockingSparqlAnswerWriter[JenaSPARQL,SyntaxType] {

      def write(answers: JenaSPARQL#Solutions, os: OutputStream) = WrappedThrowable.fromTryCatch {
        jenaSparqlSyntax.formatter.format(os, answers)
      }
      def write(answer: Boolean, os: OutputStream) = null //todo
    }

  implicit val SparqlAnswerJson: BlockingSparqlAnswerWriter[JenaSPARQL,SparqlAnswerJson] =
    JenaSparqlBlockingWriter[SparqlAnswerJson]

  implicit val SparqlAnswerXML: BlockingSparqlAnswerWriter[JenaSPARQL,SparqlAnswerXML]  =
    JenaSparqlBlockingWriter[SparqlAnswerXML]

}
