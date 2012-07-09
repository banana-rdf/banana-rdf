package org.w3.banana.util

import org.w3.banana._
import scalaz.{ Validation, Success, Failure }

trait BananaExceptionImplicits {

  def bananaCatch[T](a: => T): Validation[BananaException, T] = try {
    Success(a)
  } catch {
    case e => Failure(StoreProblem(e))
  }

}

object BananaExceptionImplicits extends BananaExceptionImplicits
