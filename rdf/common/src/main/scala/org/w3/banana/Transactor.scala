package org.w3.banana

import scala.util.Try

/**
 * A typeclass for `T`s supporting transactions.
 */
trait Transactor[Rdf <: RDF, A] {

  /** Evaluates `body` in a read transaction. */
  def r[T](a: A, body: => T): Try[T]

  /** Evaluates `body` in a read/write transaction. */
  def rw[T](a: A, body: => T): Try[T]

  val transactorSyntax = new syntax.TransactorSyntax[Rdf, A]

}
