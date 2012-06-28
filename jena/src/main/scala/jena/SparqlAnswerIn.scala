package org.w3.banana.jena

import com.hp.hpl.jena.query.{ ResultSet, ResultSetFactory }
import java.io.InputStream
import org.w3.banana.{ SparqlAnswerXML, SparqlAnswerJson }

/**
 * typeclass for serialising special
 * @tparam T
 */
trait SparqlAnswerIn[T] {
  def parse(in: InputStream): ResultSet
}

object SparqlAnswerIn {

  implicit val Json: SparqlAnswerIn[SparqlAnswerJson] =
    new SparqlAnswerIn[SparqlAnswerJson] {
      def parse(in: InputStream) = ResultSetFactory.fromJSON(in)
    }

  implicit val XML: SparqlAnswerIn[SparqlAnswerXML] =
    new SparqlAnswerIn[SparqlAnswerXML] {
      def parse(in: InputStream) = ResultSetFactory.fromXML(in)
    }

}

