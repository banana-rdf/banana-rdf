package org.w3.banana

import java.net.URL
import scalaz.Id.Id

object SparqlHttp {

  def apply[Rdf <: RDF](implicit sparqlHttp: SparqlHttp[Rdf, Id]): SparqlHttp[Rdf, Id] = sparqlHttp

}

trait SparqlHttp[Rdf <: RDF, M[_]] {

  def apply(endpoint: URL): SparqlEngine[Rdf, M]

}
