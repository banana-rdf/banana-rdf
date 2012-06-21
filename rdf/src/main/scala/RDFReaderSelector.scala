package org.w3.banana

/**
 * typeclass for a selector of RDFReader based on the provided mimetype
 */
trait RDFReaderSelector[Rdf <: RDF] extends (MimeType => Option[RDFReader[Rdf, Any]]) {

  def combineWith(other: RDFReaderSelector[Rdf]): RDFReaderSelector[Rdf] = RDFReaderSelector.combine(this, other)

}

/** RDFReaderSelector proposes some helpers to build selectors */
object RDFReaderSelector {

  def apply[Rdf <: RDF, T](implicit syntax: Syntax[T], reader: RDFReader[Rdf, T]): RDFReaderSelector[Rdf] =
    new RDFReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] =
        if (syntax.mimeTypes contains mime)
          Some(reader)
        else
          None
    }

  def combine[Rdf <: RDF](selector1: RDFReaderSelector[Rdf], selector2: RDFReaderSelector[Rdf]): RDFReaderSelector[Rdf] =
    new RDFReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] = selector1(mime) orElse selector2(mime)
    }

}
