package org.w3.banana

import scalaz.{ Validation, Success, Failure }
import scalaz.Validation._

sealed trait BananaException extends Exception

case class FailedConversion(message: String) extends Exception(message) with BananaException

case class WrongExpectation(message: String) extends Exception(message) with BananaException

case class WrappedThrowable(t: Throwable) extends Exception(t) with BananaException

object WrappedThrowable {
  def fromTryCatch[T](body: => T): Validation[WrappedThrowable, T] =
    try {
      Success(body)
    } catch {
      case t => Failure(WrappedThrowable(t))
    }
}
