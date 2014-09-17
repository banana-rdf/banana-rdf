package org.w3.banana.sesame

import org.openrdf.repository.sail.{ SailRepository, SailRepositoryConnection }

import scala.concurrent.Future
import scala.concurrent.Future.successful

object SesameUtil {

  def withConnection[T](repository: SailRepository)(func: SailRepositoryConnection => T): Future[T] = successful {
    val conn = repository.getConnection
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

}
