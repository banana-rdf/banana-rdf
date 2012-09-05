package org.w3.banana

import scalaz.{ Validation, Success, Failure }
import scalaz.Validation._
import scalaz.Semigroup.firstSemigroup

object BananaException {

  implicit val bananaExceptionSemiGroup = firstSemigroup[BananaException]

  def bananaCatch[T](a: => T): Validation[BananaException, T] = try {
    Success(a)
  } catch {
    case e => Failure(StoreProblem(e))
  }

}

trait BananaException extends Exception

case class FailedConversion(message: String) extends Exception(message) with BananaException

case class WrongExpectation(message: String) extends Exception(message) with BananaException

case class WrappedThrowable(t: Throwable) extends Exception(t) with BananaException

case class NoReader(mimetype: String) extends Exception("No RDFReader for " + mimetype) with BananaException

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

case class StoreProblem(t: Throwable) extends Exception(t) with BananaException

case object NotPureFragment extends Exception("not a pure fragment URI") with BananaException

case class BananaTimeout(te: java.util.concurrent.TimeoutException) extends Exception(te) with BananaException
