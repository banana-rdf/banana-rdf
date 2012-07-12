package org.w3.banana

import scalaz._

package object util
    extends FutureSyntax
    with ValidationSyntax
    with AnySyntax {

  type BananaValidation[T] = Validation[BananaException, T]

  type BananaFuture[T] = FutureValidation[BananaException, T]

  implicit val IdUnsafeExtractor: UnsafeExtractor[Id] = new UnsafeExtractor[Id] {
    def unsafeExtract[T](id: => Id[T]): Validation[Exception, T] =
      try {
        Success(id)
      } catch {
        case e: Exception => Failure(e)
      }
  }

}
