package org.w3.banana
package io

trait RDFWriterSelector[Rdf <: RDF, M[_]] extends (MediaRange => Option[RDFWriter[Rdf, M, Any]])

object RDFWriterSelector {

  /* this is needed because we can't have anonymous classes inside value
   * classes.
   */
  final class CombinedSelectors[Rdf <: RDF, M[_]] private [RDFWriterSelector] (
    left: RDFWriterSelector[Rdf, M],
    right: RDFWriterSelector[Rdf, M]
  ) extends RDFWriterSelector[Rdf, M] {
    def apply(range: MediaRange): Option[RDFWriter[Rdf, M, Any]] = {
      left(range) orElse right(range)
    }
  }

  implicit class RDFWriterSelectorW[Rdf <: RDF, M[_]](val selector: RDFWriterSelector[Rdf, M]) extends AnyVal {

    def combineWith(other: RDFWriterSelector[Rdf, M]): RDFWriterSelector[Rdf, M] =
      new CombinedSelectors[Rdf, M](selector, other)

  }

  def apply[Rdf <: RDF, M[_], T](implicit
    syntax: Syntax[T],
    writer: RDFWriter[Rdf, M, T]
  ): RDFWriterSelector[Rdf, M] = new RDFWriterSelector[Rdf, M] {
    def apply(range: MediaRange): Option[RDFWriter[Rdf, M, Any]] =
      syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
  }

}
