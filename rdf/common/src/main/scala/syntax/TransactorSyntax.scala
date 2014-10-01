package org.w3.banana.syntax

import org.w3.banana._

final class TransactorSyntax[Rdf <: RDF, A] {

  implicit def transactorW(a: A) = new TransactorW[Rdf, A](a)

}

final class TransactorW[Rdf <: RDF, A](val a: A) extends AnyVal {

  def r[T](body: => T)(implicit transactor: Transactor[Rdf, A]) =
    transactor.r(a, body)

  def rw[T](body: => T)(implicit transactor: Transactor[Rdf, A]) =
    transactor.rw(a, body)

}
