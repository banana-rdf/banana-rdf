package org.w3.banana

import scalaz.{Functor, Applicative, Monad, Success}
import akka.dispatch.{Promise, Future}

package object util extends FutureImplicits with BananaExceptionImplicits {

  type BananaFuture[T] = FutureValidation[BananaException, T]

  implicit def BananaFutureMonad: Monad[BananaFuture] = new Monad[BananaFuture] {
    def point[A](x: => A): BananaFuture[A] = FutureImplicits.validationToFutureValidation(Success(x))
    override def map[A, B](x: BananaFuture[A])(f: A => B): BananaFuture[B] = x map f
    def bind[A, B](x: BananaFuture[A])(f: A => BananaFuture[B]): BananaFuture[B] = x flatMap f
  }

  import FutureImplicits._
  implicit def FutureMonad: Monad[Future] = new Monad[Future] {
    def bind[A, B](fa: Future[A])(f: (A) => Future[B]): Future[B] = fa flatMap f
    def point[A](a: => A): Future[A] = Future(a)
    override def map[A,B](fa: Future[A])(f: A => B): Future[B] = fa map f
  }
}
