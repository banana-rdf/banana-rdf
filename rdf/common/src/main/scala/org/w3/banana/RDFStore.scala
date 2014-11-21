package org.w3.banana

trait RDFStore[Rdf <: RDF, M[+_], A]
  extends SparqlEngine[Rdf, M, A]
  with GraphStore[Rdf, M, A]
  with Transactor[Rdf, A]

object RDFStore {

  def apply[Rdf <: RDF, M[+_], A](implicit rdfStore: RDFStore[Rdf, M, A]): RDFStore[Rdf, M, A] = rdfStore

}
