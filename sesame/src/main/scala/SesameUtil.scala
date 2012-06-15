package org.w3.banana.sesame

import org.openrdf.repository.{ Repository, RepositoryConnection }
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.{QueryEvaluationException, BindingSet}
import org.openrdf.model.Statement
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.SailConnection

object SesameUtil {

  type QueryResult[T<: BindingSet] =  CloseableIteration[T, QueryEvaluationException]

  def withConnection[T](repository: SailRepository)(func: SailConnection => T): T = {
    val conn = repository.getConnection.getSailConnection
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

  def toIterator[T<: BindingSet](queryResult: QueryResult[T]): Iterator[T] = new Iterator[T] {
    def hasNext: Boolean = queryResult.hasNext
    def next(): T = queryResult.next()
  }

  def toIterable[T<: BindingSet](queryResult: QueryResult[T]): Iterable[T] = new Iterable[T] {
    def iterator = SesameUtil.toIterator(queryResult)
  }

  def toStatementIterable[T<: BindingSet](queryResult: QueryResult[T]): Iterable[Statement] = new Iterable[Statement] {
    def iterator = new Iterator[Statement] {
      def hasNext: Boolean = queryResult.hasNext
      def next(): Statement = queryResult.next().asInstanceOf[Statement]
    }
  }

  def toPartialFunction(bs: BindingSet): PartialFunction[String, Sesame#Node] =
    new PartialFunction[String, Sesame#Node] {
      def apply(v: String): Sesame#Node = bs.getValue(v)
      def isDefinedAt(v: String): Boolean = bs.hasBinding(v)
    }

}
