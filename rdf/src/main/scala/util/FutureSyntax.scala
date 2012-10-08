package org.w3.banana.util

import org.w3.banana.BananaException

import scala.concurrent._
import akka.dispatch._
import scala.concurrent.util._
import scalaz.Validation
import scalaz.syntax.validation._

trait FutureSyntax /*extends AkkaDefaults*/ {

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

  implicit def futureOfValidationToFutureValidation[F, S](in: Future[Validation[F, S]]): FutureValidation[F, S] =
    FutureValidation(in)

  implicit def validationToFutureValidation[F, S](in: Validation[F, S])(implicit ec: ExecutionContext): FutureValidation[F, S] =
    FutureValidation(Future.successful(in))

}

object FutureSyntax extends FutureSyntax
