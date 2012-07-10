package org.w3.banana

import scalaz.{Monad, Bind, Success}
import akka.dispatch.{Promise, Future}

package object util extends FutureImplicits with BananaExceptionImplicits {

  type BananaFuture[T] = FutureValidation[BananaException, T]

  implicit val BananaFuturBind: Bind[BananaFuture] = new Bind[BananaFuture] {
    override def map[A, B](x: BananaFuture[A])(f: A => B): BananaFuture[B] = x map f
    def bind[A, B](x: BananaFuture[A])(f: A => BananaFuture[B]): BananaFuture[B] = x flatMap f
  }

}
