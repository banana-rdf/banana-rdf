package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.sparql.resultset.{ JSONOutput, XMLOutput }
import java.io.{ Reader, InputStream, OutputStream }
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
 * Creates a blocking SPARQL BlockingReader for the given syntax
 */
object JenaQueryResultsReader {

  type Answer = Either[Jena#Solutions, Boolean]

  def apply[S](implicit jenaSparqlSyntax: SparqlAnswerIn[S]): SparqlQueryResultsReader[Jena, S] =
    new SparqlQueryResultsReader[Jena, S] {

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

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    JenaQueryResultsReader[SparqlAnswerJson]

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    JenaQueryResultsReader[SparqlAnswerXml]

  //implicit val queryResultsReaderSelector: ReaderSelector[Jena, Any] =
  //   ReaderSelector[Jena, SparqlAnswerJson] combineWith ReaderSelector[Jena, SparqlAnswerXml]

}
