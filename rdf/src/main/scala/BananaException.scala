package org.w3.banana

import scalaz.{ Validation, Success, Failure }
import scalaz.Validation._

object BananaException {

  implicit val validationBananaExceptionMonad = validationMonad[BananaException]

}

sealed trait BananaException extends Exception

case class FailedConversion(message: String) extends Exception(message) with BananaException

case class WrongExpectation(message: String) extends Exception(message) with BananaException

case class WrappedThrowable(t: Throwable) extends Exception(t) with BananaException

object WrappedThrowable {
  def fromTryCatch[T](body: => T): Validation[WrappedThrowable, T] =
    try {
      Success(body)
    } catch {
      // only catch exceptions that should be caught, let VM throwables through.
      case t: Exception => Failure(WrappedThrowable(t))
    }
}

case class LocalNameException(message: String) extends Exception(message) with BananaException

case class VarNotFound(message: String) extends Exception(message) with BananaException
