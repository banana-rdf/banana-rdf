package org.w3.banana

import scalaz._
import Id._

package object util
    extends FutureSyntax
    with ValidationSyntax
    with AnySyntax {

  implicit val IdUnsafeExtractor: UnsafeExtractor[Id] = new UnsafeExtractor[Id] {
    def unsafeExtract[T](id: => Id[T]): Validation[Exception, T] =
      try {
        Success(id)
      } catch {
        case e: Exception => Failure(e)
      }
  }

}
