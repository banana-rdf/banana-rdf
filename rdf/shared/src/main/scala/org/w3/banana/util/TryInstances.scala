package org.w3.banana
package util

import scalaz.{ Monad, Comonad }
import scala.util.Try

object tryInstances {
  implicit final val TryInstance: Monad[Try] with Comonad[Try] = new Monad[Try] with Comonad[Try] {
    def point[A](a: => A): Try[A] = Try[A](a)
    def bind[A, B](fa: Try[A])(f: A => Try[B]): Try[B] = fa flatMap f
    override def map[A, B](fa: Try[A])(f: A => B): Try[B] = fa map f
    def cobind[A, B](fa: Try[A])(f: Try[A] => B): Try[B] = Try[B](f(fa))
    override def cojoin[A](a: Try[A]): Try[Try[A]] = Try[Try[A]](a)
    def copoint[A](p: Try[A]): A = p.get
  }
}
