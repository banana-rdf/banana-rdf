package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.{ Repository, RepositoryConnection }
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.{ QueryEvaluationException, BindingSet }
import org.openrdf.model.Statement
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.SailConnection

object SesameUtil {

  def withConnection[T](repository: SailRepository)(func: SailConnection => T): T = {
    val conn = repository.getConnection.getSailConnection
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

  type QueryResult[T <: BindingSet] = CloseableIteration[T, QueryEvaluationException]

  def toStatementIterable[T <: BindingSet](queryResult: QueryResult[T]): Iterable[Statement] = new Iterable[Statement] {
    def iterator = new Iterator[Statement] {
      def hasNext: Boolean = queryResult.hasNext
      def next(): Statement = queryResult.next().asInstanceOf[Statement]
    }
  }

}
