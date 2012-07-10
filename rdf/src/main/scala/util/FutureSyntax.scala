package org.w3.banana.util

import org.w3.banana.BananaException

import akka.dispatch.{ Await, Future, Promise }
import akka.util.Duration
import scalaz.Validation
import scalaz.syntax.validation._

trait FutureSyntax extends AkkaDefaults {

  class FutureW[A](inner: Future[A]) {
    implicit val defaultDuration = Duration("3s")

    def await: A = await()

    def await(duration: Duration = defaultDuration): A =
      Await.result(inner, duration)

    def awaitOption: Option[A] = awaitOption()

    def awaitOption(duration: Duration = defaultDuration): Option[A] =
      try {
        Some(await(duration))
      } catch {
        case exn: Exception => None
      }

  }

  implicit def futureToFutureW[A](f: Future[A]) =
    new FutureW(f)

  //  def delayFailure[F, S](in: Validation[F, Future[S]]): Future[Validation[F, S]] =
  //    in fold (
  //      failure = f => Promise.successful(f.fail[S]),
  //      success = s => s map (_.success[F])
  //    )
  //
  //  def flattenValidations[F, S](in: Validation[F, Validation[F, S]]): Validation[F, S] =
  //    in fold (
  //      failure = f => f.fail[S],
  //      success = s => s
  //    )

  implicit def futureOfValidationToFutureValidation[F, S](in: Future[Validation[F, S]]): FutureValidation[F, S] =
    FutureValidation(in)

  implicit def validationToFutureValidation[F, S](in: Validation[F, S]): FutureValidation[F, S] =
    FutureValidation(Promise.successful(in))

}

object FutureSyntax extends FutureSyntax
