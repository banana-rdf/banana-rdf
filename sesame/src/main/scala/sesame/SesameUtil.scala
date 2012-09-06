package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.sail.{ SailRepository, SailRepositoryConnection }

object SesameUtil {

  def withConnection[T](repository: SailRepository)(func: SailRepositoryConnection => T): T = {
    val conn = repository.getConnection
    val result = func(conn)
    conn.commit()
    conn.close()
    result
  }

}
