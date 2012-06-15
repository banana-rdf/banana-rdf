package org.w3.banana.sesame

import org.openrdf.repository.{ Repository, RepositoryConnection }
import org.openrdf.query.{ QueryResult, BindingSet }

object SesameUtil {

  def withConnection[T](repository: Repository)(func: RepositoryConnection => T): T = {
    val conn = repository.getConnection()
    conn.setAutoCommit(false)
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

  def toIterator[T](queryResult: QueryResult[T]): Iterator[T] = new Iterator[T] {
    def hasNext: Boolean = queryResult.hasNext
    def next(): T = queryResult.next()
  }

  def toIterable[T](queryResult: QueryResult[T]): Iterable[T] = new Iterable[T] {
    def iterator = SesameUtil.toIterator(queryResult)
  }

  def toPartialFunction(bs: BindingSet): PartialFunction[String, Sesame#Node] =
    new PartialFunction[String, Sesame#Node] {
      def apply(v: String): Sesame#Node = bs.getValue(v)
      def isDefinedAt(v: String): Boolean = bs.hasBinding(v)
    }

}
