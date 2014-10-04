package org.w3.banana

object RDFWriterSelector {

  def apply[Rdf <: RDF, T](implicit syntax: Syntax[T],
    writer: RDFWriter[Rdf, T]): RDFWriterSelector[Rdf] = new RDFWriterSelector[Rdf] {
    def apply(range: MediaRange): Option[RDFWriter[Rdf, Any]] =
      syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
  }

}
