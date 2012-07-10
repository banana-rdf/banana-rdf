package org.w3.banana.util

import org.w3.banana.BananaException

import akka.dispatch.Promise
import scalaz.{ Validation, Success }

class AnyW[T](t: T) {
  def bf: BananaFuture[T] = FutureValidation(Promise.successful(Success(t)))
}

trait AnySyntax {
  implicit def anyToAnyW[T](t: T): AnyW[T] = new AnyW(t)
}

object AnySyntax extends AnySyntax
