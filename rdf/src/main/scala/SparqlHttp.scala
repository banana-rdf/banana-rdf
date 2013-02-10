package org.w3.banana

import java.net.URL

trait SparqlHttp[Rdf <: RDF, M[_]] {

  def apply(endpoint: URL): SparqlEngine[Rdf, M]

}
