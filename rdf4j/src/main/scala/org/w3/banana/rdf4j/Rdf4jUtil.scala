package org.w3.banana.rdf4j

import org.eclipse.rdf4j.repository.sail.{SailRepository, SailRepositoryConnection}

import scala.concurrent.Future
import scala.concurrent.Future.successful

object Rdf4jUtil {

  def withConnection[T](repository: SailRepository)(func: SailRepositoryConnection => T): Future[T] = successful {
    val conn = repository.getConnection
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

}
