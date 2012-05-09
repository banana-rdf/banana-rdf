package org.w3.banana.sesame

import org.openrdf.repository.{ Repository, RepositoryConnection }
import org.openrdf.query.QueryResult

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
}
