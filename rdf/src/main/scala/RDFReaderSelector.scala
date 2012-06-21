package org.w3.banana

/**
 * typeclass for a selector of RDFReader based on the provided mimetype
 */
trait RDFReaderSelector[Rdf <: RDF] extends (MimeType => Option[RDFReader[Rdf, Any]])

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

  def apply[Rdf <: RDF, T1, T2](implicit syntax1: Syntax[T1], reader1: RDFReader[Rdf, T1], syntax2: Syntax[T2], reader2: RDFReader[Rdf, T2]): RDFReaderSelector[Rdf] =
    new RDFReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] =
        if (syntax1.mimeTypes contains mime)
          Some(reader1)
        else if (syntax2.mimeTypes contains mime)
          Some(reader2)
        else
          None
    }

  def apply[Rdf <: RDF, T1, T2, T3](implicit syntax1: Syntax[T1], reader1: RDFReader[Rdf, T1], syntax2: Syntax[T2], reader2: RDFReader[Rdf, T2], syntax3: Syntax[T3], reader3: RDFReader[Rdf, T3]): RDFReaderSelector[Rdf] =
    new RDFReaderSelector[Rdf] {
      def apply(mime: MimeType): Option[RDFReader[Rdf, Any]] =
        if (syntax1.mimeTypes contains mime)
          Some(reader1)
        else if (syntax2.mimeTypes contains mime)
          Some(reader2)
        else if (syntax3.mimeTypes contains mime)
          Some(reader3)
        else
          None
    }

}
