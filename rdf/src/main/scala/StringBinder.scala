package org.w3.banana

import scalaz.Validation

trait StringBinder[T] {
  def fromString(s: String): Validation[BananaException, T]
  def toString(t: T): String
}

object StringBinder {

  def apply[T](f: String => Validation[BananaException, T]): StringBinder[T] =
    new StringBinder[T] {
      def fromString(s: String): Validation[BananaException, T] = f(s)
      def toString(t: T): String = t.toString()
    }

}
