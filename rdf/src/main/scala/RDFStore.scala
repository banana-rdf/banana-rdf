package org.w3.banana

trait RDFStore[Rdf <: RDF, A]
  extends SparqlEngine[Rdf, A]
  with GraphStore[Rdf, A]
  with Transactor[Rdf, A]

object RDFStore {

  def apply[Rdf <: RDF, A](implicit rdfStore: RDFStore[Rdf, A]): RDFStore[Rdf, A] = rdfStore

}
