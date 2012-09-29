package org.w3.banana

trait WriterSelector[O] extends (MediaRange => Option[Writer[O, Any]]) { self =>

  def combineWith(otherSelector: WriterSelector[O]): WriterSelector[O] =
    new WriterSelector[O] {
      def apply(range: MediaRange): Option[Writer[O, Any]] = {
        self(range) orElse otherSelector(range)
      }
    }

}

object WriterSelector {

  def apply[O, T](implicit syntax: Syntax[T], writer: Writer[O, T]): WriterSelector[O] =
    new WriterSelector[O] {
      def apply(range: MediaRange): Option[Writer[O, Any]] =
        syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
    }

}

