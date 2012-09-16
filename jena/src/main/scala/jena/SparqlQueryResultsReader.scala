package org.w3.banana.jena

import com.hp.hpl.jena.sparql.resultset.{ JSONOutput, XMLOutput }
import java.io.{ Reader, InputStream, OutputStream }
import org.w3.banana._
import jena._
import scalaz.{ Validation, Either3 }
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Creates a blocking SPARQL BlockingReader for the given syntax
 */
object SparqlQueryResultsReader {

  type Answer = Either[Jena#Solutions, Boolean]

  def apply[Syntax](implicit jenaSparqlSyntax: SparqlAnswerIn[Syntax]): SparqlQueryResultsReader[Jena, Syntax] =
    new SparqlQueryResultsReader[Jena, Syntax] {

      def read(in: InputStream, base: String = ""): BananaValidation[Answer] =
        WrappedThrowable.fromTryCatch {
          val resultSet = jenaSparqlSyntax.parse(in)
          if (resultSet.isBoolean) {
            Right(resultSet.getBooleanResult)
          } else if (resultSet.isResultSet) {
            Left(resultSet.getResultSet)
          } else {
            throw new WrongExpectation("was expecting either a boolean or result set answer. received a model? " + resultSet.isModel)
          }
        }

      def read(reader: Reader, base: String) = throw new NotImplementedException
    }

  implicit val JsonSparqlQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    SparqlQueryResultsReader[SparqlAnswerJson]

  implicit val XMLSparqlQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerXML] =
    SparqlQueryResultsReader[SparqlAnswerXML]

  implicit val ReaderSelector: ReaderSelector[Answer] =
    ReaderSelector2[Answer, SparqlAnswerJson] combineWith ReaderSelector2[Answer, SparqlAnswerXML]

}
