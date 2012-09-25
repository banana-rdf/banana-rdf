package org.w3.banana

trait BananaSPARQLSolutionWriterSelector {

  object SPARQLSolutionWriterSelector {

    def apply[Rdf <: RDF, T](implicit syntax: Syntax[T], writer: SparqlSolutionsWriter[Rdf, T]): SparqlSolutionsWriterSelector[Rdf] =
      new SparqlSolutionsWriterSelector[Rdf] {
        def apply(range: MediaRange): Option[SparqlSolutionsWriter[Rdf, Any]] =
          syntax.mimeTypes.list.find(m => range.matches(m)).map(_ => writer)
      }
  }

}
