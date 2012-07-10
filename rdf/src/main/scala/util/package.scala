package org.w3.banana

import scalaz._
import akka.dispatch.{Promise, Future}

package object util extends FutureImplicits with BananaExceptionImplicits {

  type BananaFuture[T] = FutureValidation[BananaException, T]

  implicit val BananaFutureBind: Bind[BananaFuture] = new Bind[BananaFuture] {
    override def map[A, B](x: BananaFuture[A])(f: A => B): BananaFuture[B] = x map f
    def bind[A, B](x: BananaFuture[A])(f: A => BananaFuture[B]): BananaFuture[B] = x flatMap f
  }

  implicit val BananaFutureUnsafeExtractor: UnsafeExtractor[BananaFuture] = new UnsafeExtractor[BananaFuture] {
    def unsafeExtract[T](bf: => BananaFuture[T]): Validation[Throwable, T] =
      try {
        bf.await()
      } catch {
        case t => Failure(t)
      }
  }

  implicit val IdUnsafeExtractor: UnsafeExtractor[Id] = new UnsafeExtractor[Id] {
    def unsafeExtract[T](id: => Id[T]): Validation[Throwable, T] =
      try {
        Success(id)
      } catch {
        case t => Failure(t)
      }
  }

}
