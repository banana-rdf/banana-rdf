package org.w3.banana
package io

trait WriterSelector[O,M[_]] extends (MediaRange => Option[Writer[O, M, Any]]) { self =>

  def combineWith(otherSelector: WriterSelector[O,M]): WriterSelector[O,M] =
    new WriterSelector[O,M] {
      def apply(range: MediaRange): Option[Writer[O, M, Any]] = {
        self(range) orElse otherSelector(range)
      }
    }

}

object WriterSelector {

  def apply[O, M[_], T](implicit syntax:  Syntax[T], writer: Writer[O, M, T]): WriterSelector[O,M] =
    new WriterSelector[O,M] {
      def apply(range: MediaRange): Option[Writer[O, M, Any]] =
        syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
    }

}

