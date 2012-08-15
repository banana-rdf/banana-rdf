package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.repository.{ Repository, RepositoryConnection }
import info.aduna.iteration.CloseableIteration
import org.openrdf.query.{ QueryEvaluationException, BindingSet }
import org.openrdf.model.{ URI, Resource, ValueFactory, Statement }
import org.openrdf.repository.sail.{ SailRepository, SailRepositoryConnection }
import org.openrdf.sail.SailConnection
import scalaz.{ Validation, Success, Failure }
import scala.collection.JavaConverters._
import org.openrdf.model.impl.StatementImpl

object SesameUtil {

  /**
   * note: The connection is not closed by this method  (as that removes the results from
   * the closeable iterator ).
   */
  def withConnection[T](repository: SailRepository)(func: SailRepositoryConnection => T): T = {
    val conn = repository.getConnection
    val result = func(conn)
    conn.commit()
    result
  }

}
