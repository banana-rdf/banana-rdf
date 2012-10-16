package org.w3.banana

import scalaz.Free

trait RDFStore[Rdf <: RDF, M[_]] {
  def execute[A](script: Free[({ type l[+x] = Command[Rdf, x] })#l, A]): M[A]
  def shutdown(): Unit
}

object RDFStore {

  implicit class RDFStoreW[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M]) {
    def asGraphStore(implicit ops: RDFOps[Rdf]): GraphStore[Rdf, M] = GraphStore[Rdf, M](store)
  }

  implicit def RDFStore2GraphStore[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M])(implicit ops: RDFOps[Rdf]): GraphStore[Rdf, M] =
    GraphStore[Rdf, M](store)

  implicit def RDFStore2SparqlEngine[Rdf <: RDF, M[_]](store: RDFStore[Rdf, M]): SparqlEngine[Rdf, M] =
    SparqlEngine[Rdf, M](store)

}
