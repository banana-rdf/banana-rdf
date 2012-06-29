package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.{ Repository, RepositoryConnection }
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.{ QueryEvaluationException, BindingSet }
import org.openrdf.model.{ URI, Resource, ValueFactory, Statement }
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.SailConnection
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._
import org.openrdf.model.impl.StatementImpl

object SesameUtil {

  type QueryResult[T <: BindingSet] = CloseableIteration[T, QueryEvaluationException]

  /**
   * note: The connection is not closed by this method  (as that removes the results from
   * the closeable iterator ).
   */
  def withConnection[T](repository: SailRepository)(func: SailConnection => T): T = {
    val conn = repository.getConnection.getSailConnection
    val result = func(conn)
    conn.commit()
    result
  }

  def toIterator[T <: BindingSet](queryResult: QueryResult[T]): Iterator[T] = new Iterator[T] {
    def hasNext: Boolean = queryResult.hasNext
    def next(): T = queryResult.next()
  }

  def toStatementIterable[T <: BindingSet](queryResult: QueryResult[T]): Iterable[Statement] = new Iterable[Statement] {
    def iterator = new Iterator[Statement] {
      def hasNext: Boolean = queryResult.hasNext
      def next(): Statement = {
        val binding = queryResult.next()
        val subj = binding.getBinding("subject").getValue.asInstanceOf[Resource]
        val pred = binding.getBinding("predicate").getValue.asInstanceOf[URI]
        val obj = binding.getBinding("object").getValue
        new StatementImpl(subj, pred, obj)
      }
    }
  }

}
