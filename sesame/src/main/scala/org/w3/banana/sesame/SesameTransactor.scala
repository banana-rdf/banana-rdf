package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana.{Transactor, SparqlUpdate, RDFStore}

import scala.util.{Failure, Try}

trait SesameTransactor extends Transactor[Sesame,RepositoryConnection] {

  def r[T](conn: RepositoryConnection, body: => T): Try[T] = Try{
    val result = body
    conn.close()
    result
  }

  def rw[T](conn: RepositoryConnection, body: => T): Try[T] = Try{
    conn.begin()
    val result = body
    conn.commit()
    conn.close()
    result
  }.recoverWith{ case exception=>
    conn.rollback()
    conn.close()
    Failure(exception)
  }

}
