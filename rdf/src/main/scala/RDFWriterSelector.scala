package org.w3.banana.jena

import org.w3.banana._
import scala.Some

trait RDFWriterSelector[Obj] extends (MediaRange => Option[BlockingWriter[Obj, Any]]) {

  def combineWith(other: RDFWriterSelector[Obj]): RDFWriterSelector[Obj] = RDFWriterSelector.combine(this, other)

}

/** ReaderWriterSelector proposes some helpers to build selectors */
object RDFWriterSelector {

  def apply[Obj, T](implicit syntax: Syntax[T], writer: BlockingWriter[Obj, T]): RDFWriterSelector[Obj] =
    new RDFWriterSelector[Obj] {
      def apply(range: MediaRange): Option[BlockingWriter[Obj, Any]] =
        syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
    }

  def combine[Obj](selector1: RDFWriterSelector[Obj], selector2: RDFWriterSelector[Obj]): RDFWriterSelector[Obj] =
    new RDFWriterSelector[Obj] {
      def apply(range: MediaRange): Option[BlockingWriter[Obj, Any]] = selector1(range) orElse selector2(range)
    }

}

