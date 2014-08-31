package org.w3.banana.util

import scala.concurrent.Future
import scala.util.control.NonFatal

trait ImmediateFuture {

  def immediate[T](body: => T): Future[T] =
    try {
      Future.successful(body)
    } catch {
      case NonFatal(t) => Future.failed(t)
    }

}

object ImmediateFuture extends ImmediateFuture
