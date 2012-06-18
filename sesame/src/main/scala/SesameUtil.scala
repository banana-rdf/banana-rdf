package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.{ Repository, RepositoryConnection }
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.{QueryEvaluationException, BindingSet}
import org.openrdf.model.Statement
import org.openrdf.repository.sail.SailRepository
import org.openrdf.sail.SailConnection
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._

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

  def toRow(bs: BindingSet): Row[Sesame] =
    new Row[Sesame] {

      def apply(v: String): Validation[BananaException, Sesame#Node] = {
        val node = bs.getValue(v)
        if (node == null)
          Failure(VarNotFound("var " + v + " not found in BindingSet " + bs.toString))
        else
          Success(node)
      }

      def vars: Iterable[String] = bs.getBindingNames.asScala

    }

}
