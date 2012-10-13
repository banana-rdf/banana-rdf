package org.w3.banana

import scala.util._

trait StringBinder[T] {
  def fromString(s: String): Try[T]
  def toString(t: T): String
}

object StringBinder {

  def apply[T](f: String => Try[T]): StringBinder[T] =
    new StringBinder[T] {
      def fromString(s: String): Try[T] = f(s)
      def toString(t: T): String = t.toString()
    }

}
