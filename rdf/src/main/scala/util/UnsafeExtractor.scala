package org.w3.banana.util

import scalaz.Validation

trait UnsafeExtractor[M[_]] {

  def unsafeExtract[T](m: => M[T]): Validation[Throwable, T]

}
