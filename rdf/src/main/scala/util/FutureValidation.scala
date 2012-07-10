package org.w3.banana.util

import akka.dispatch._
import akka.util.Duration
import scalaz.Validation
import scalaz.syntax.validation._

case class FutureValidation[F, S](inner: Future[Validation[F, S]]) extends AkkaDefaults {

  implicit val defaultDuration = Duration("3s")

  def map[T](fn: (S) => T): FutureValidation[F, T] =
    FutureValidation(inner map { validation => validation map fn })

  def flatMap[T](fn: (S) => FutureValidation[F, T]): FutureValidation[F, T] =
    FutureValidation(
      inner flatMap { validation =>
        validation fold (
          failure = f => Promise.successful(f.fail[T]),
          success = s => fn(s).inner
        )
      })

  def fold[T](failure: (F) => T = identity[F] _, success: (S) => T = identity[S] _): Future[T] =
    inner map { validation => validation fold (failure = failure, success = success) }

  def mapFailure[G](f: F => G): FutureValidation[G, S] =
    FutureValidation(
      this.fold(
        failure = f(_).fail,
        success = x => x.success
      )
    )

  def foreach[T](f: (S) => T): Unit = {
    this.map(f)
    ()
  }

  def orElse[G](f: F => FutureValidation[G, S]) =
    inner flatMap {
      (v: Validation[F, S]) =>
        v.fold(
          success = s => Promise.successful(s.success[G]),
          failure = f(_).inner
        )
    } fv

  /** And the success shall be failures and the failures shall be successes. This is how you do logical negation */
  def invert: FutureValidation[S, F] =
    FutureValidation(
      inner map (v => v fold (
        success = s => s.fail,
        failure = f => f.success))
    )

  def fv: FutureValidation[F, S] =
    this

  def await(duration: Duration = defaultDuration): Validation[F, S] =
    Await.result(inner, duration)

  def awaitSuccess(duration: Duration = defaultDuration): S =
    await(duration).toOption.get

}

import scalaz._

object FutureValidation {

  implicit def FutureValidationBind[E]: Bind[({ type l[x] = FutureValidation[E, x] })#l] = new Bind[({ type l[x] = FutureValidation[E, x] })#l] {
    override def map[A, B](x: FutureValidation[E, A])(f: A => B): FutureValidation[E, B] = x map f
    def bind[A, B](x: FutureValidation[E, A])(f: A => FutureValidation[E, B]): FutureValidation[E, B] = x flatMap f
  }

  implicit def BananaFutureUnsafeExtractor[E]: UnsafeExtractor[({ type l[x] = FutureValidation[E, x] })#l] = new UnsafeExtractor[({ type l[x] = FutureValidation[E, x] })#l] {
    def unsafeExtract[T](bf: => FutureValidation[E, T]): Validation[Exception, T] =
      try {
        bf.await().fold(e => Failure(new Exception(e.toString)), Success(_))
      } catch {
        case e: Exception => Failure(e)
      }
  }

}
