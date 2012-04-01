package org.w3.util

import akka.dispatch._
import akka.util.duration._
import akka.util.Duration
import scalaz._
import Scalaz._
import java.util.concurrent.TimeUnit

object FutureValidation {

  def apply[F, S](futureValidation: Future[Validation[F, S]]): FutureValidation[F, S] =
    new FutureValidation(futureValidation)

  def delayedValidation[F, S](body: => Validation[F, S])(implicit context: ExecutionContext): FutureValidation[F, S] =
    FutureValidation(Future(body))

  def immediateValidation[F, S](body: => Validation[F, S])(implicit context: ExecutionContext): FutureValidation[F, S] =
    FutureValidation(Promise.successful(body))

}

/**
 * the combination of a Future and a Validation
 */
class FutureValidation[+F, +S] private (private val futureValidation: Future[Validation[F, S]]) {

  /**
   * DO NOT CALL THIS OUTSIDE OF TESTS
   */
  def waitResult(): S = {
    Await.result(futureValidation, 3.seconds) match {
      case Failure(f) => sys.error(f.toString)
      case Success(s) => s
    }
  }

  /**
   * combines the computations in the future while respecting the semantics of the Validation
   */
  def flatMap[FF >: F, T](f: S => FutureValidation[FF, T])(implicit executor: ExecutionContext): FutureValidation[FF, T] = {
    val futureResult = futureValidation.flatMap[Validation[FF, T]] {
      case Failure(failure) => Promise.successful(Failure(failure))
      case Success(value) => f(value).futureValidation
    }
    new FutureValidation(futureResult)
  }

  def map[T](f: S => T): FutureValidation[F, T] = 
    new FutureValidation(futureValidation map { _ map f })

  def foreach(f: S => Unit): Unit =
    futureValidation foreach { _ foreach f }

  def failMap[T](f: F => T): FutureValidation[T, S] =
    new FutureValidation(futureValidation map { _.fail.map(f).validation })

  def isCompleted: Boolean = futureValidation.isCompleted

  def asFuture: Future[Validation[F, S]] = futureValidation

  def toFuture[T](
    implicit evF: F <:< T,
    evS: S <:< T): Future[T] = futureValidation.map{ _.fold(evF, evS) }

}

