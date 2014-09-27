package org.w3.banana

/**
 * typeclass for a selector of RDFReader based on the provided mimetype
 */
trait ReaderSelector[Rdf <: RDF] extends (MimeType => Option[RDFReader[Rdf, Any]]) {

  def combineWith(otherSelector: ReaderSelector[Rdf]): ReaderSelector[Rdf] =
    new ReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] = {
        ReaderSelector.this(mime) orElse otherSelector(mime)
      }
    }

}

object ReaderSelector {

  def apply[Rdf <: RDF, S](implicit syntax: Syntax[S], reader: RDFReader[Rdf, S]): ReaderSelector[Rdf] =
    new ReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] =
        if (syntax.mimeTypes.list contains mime)
          Some(reader)
        else
          None
    }

}
