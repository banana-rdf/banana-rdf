package org.w3.banana
package io

/** A selector for [[RDFReader]]s based on the provided mimetype. */
trait ReaderSelector[Rdf <: RDF, M[_]] extends (MimeType => Option[RDFReader[Rdf, M, Any]]) {

  def combineWith(otherSelector: ReaderSelector[Rdf, M]): ReaderSelector[Rdf, M] = new ReaderSelector[Rdf, M] {
    def apply(mime: MimeType): Option[RDFReader[Rdf, M, Any]] = {
      ReaderSelector.this(mime) orElse otherSelector(mime)
    }
  }

}

object ReaderSelector {

  def apply[Rdf <: RDF, M[_], S](implicit
    syntax: Syntax[S],
    reader: RDFReader[Rdf, M, S]
  ): ReaderSelector[Rdf, M] = new ReaderSelector[Rdf, M] {
    def apply(mime: MimeType): Option[RDFReader[Rdf, M, Any]] =
      if (syntax.mimeTypes.list contains mime) Some(reader)
      else  None
  }

}
