package org.w3.banana

import java.net.URL
import scalaz.Id.Id

object SparqlHttp {

  def apply[Rdf <: RDF](implicit sparqlHttp: SparqlHttp[Rdf]): SparqlHttp[Rdf] = sparqlHttp

}

trait SparqlHttp[Rdf <: RDF] {

  def apply(endpoint: URL): SparqlEngine[Rdf]

}
