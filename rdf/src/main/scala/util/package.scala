package org.w3.banana

import scalaz.{ Monad, Success }

package object util {

  type BananaFuture[T] = FutureValidation[BananaException, T]

  implicit def BananaFutureMonad: Monad[BananaFuture] = new Monad[BananaFuture] {
    def point[A](x: => A): BananaFuture[A] = FutureImplicits.validationToFutureValidation(Success(x))
    override def map[A, B](x: BananaFuture[A])(f: A => B): BananaFuture[B] = x map f
    def bind[A, B](x: BananaFuture[A])(f: A => BananaFuture[B]): BananaFuture[B] = x flatMap f
  }


}
