package org.w3.banana
package io

import scala.util.Try

trait WriterSelector[O] extends (MediaRange => Option[Writer[O, Try, Any]]) { self =>

  def combineWith(otherSelector: WriterSelector[O]): WriterSelector[O] =
    new WriterSelector[O] {
      def apply(range: MediaRange): Option[Writer[O, Try, Any]] = {
        self(range) orElse otherSelector(range)
      }
    }

}

object WriterSelector {

  def apply[O, T](implicit syntax:  Syntax[T], writer: Writer[O, Try, T]): WriterSelector[O] =
    new WriterSelector[O] {
      def apply(range: MediaRange): Option[Writer[O, Try, Any]] =
        syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
    }

}

