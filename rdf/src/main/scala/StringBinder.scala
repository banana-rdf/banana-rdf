package org.w3.banana

import scalaz.Validation

trait StringBinder[T] {
  def fromString(s: String): BananaValidation[T]
  def toString(t: T): String
}

object StringBinder {

  def apply[T](f: String => BananaValidation[T]): StringBinder[T] =
    new StringBinder[T] {
      def fromString(s: String): BananaValidation[T] = f(s)
      def toString(t: T): String = t.toString()
    }

}
