import com.hp.hpl.jena.sparql.resultset.{JSONOutput, XMLOutput}
import java.io.{InputStream, OutputStream}
import org.w3.banana._
import jena._
import scalaz.Either3

/**
 * Creates a blocking SPARQL writer for the given syntax
 */
object SparqlAnswerReader {

  def apply[Syntax](implicit jenaSparqlSyntax: SparqlAnswerIn[Syntax]):
  BlockingSparqlAnswerReader[JenaSPARQL, Syntax] =
    new BlockingSparqlAnswerReader[JenaSPARQL, Syntax] {

      def read(in: InputStream) = WrappedThrowable.fromTryCatch{
        jenaSparqlSyntax.parse(in)
      }
    }

  implicit val Json: BlockingSparqlAnswerReader[JenaSPARQL, SparqlAnswerJson] =
    SparqlAnswerReader[SparqlAnswerJson]

  implicit val XML: BlockingSparqlAnswerReader[JenaSPARQL, SparqlAnswerXML] =
    SparqlAnswerReader[SparqlAnswerXML]

}
