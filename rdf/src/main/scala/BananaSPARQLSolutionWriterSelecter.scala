package org.w3.banana

trait BananaSPARQLSolutionWriterSelector {

  object SPARQLSolutionWriterSelector {

    def apply[Rdf <: RDF, T](implicit syntax: Syntax[T], writer: SPARQLSolutionsWriter[Rdf, T]): SPARQLSolutionsWriterSelector[Rdf] =
      new SPARQLSolutionsWriterSelector[Rdf] {
        def apply(range: MediaRange): Option[SPARQLSolutionsWriter[Rdf, Any]] =
          syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
      }
  }

}
