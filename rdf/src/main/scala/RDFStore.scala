package org.w3.banana

import scalaz.Free
import scala.concurrent.Future

trait RDFStore[Rdf <: RDF] {
  def execute[A](script: Free[({ type l[+x] = Command[Rdf, x] })#l, A]): Future[A]
  def shutdown(): Unit
}

object RDFStore {

  implicit class RDFStoreW[Rdf <: RDF](store: RDFStore[Rdf]) {
    def asGraphStore(implicit ops: RDFOps[Rdf]): GraphStore[Rdf] = GraphStore[Rdf](store)
  }

  implicit def RDFStore2GraphStore[Rdf <: RDF](store: RDFStore[Rdf])(implicit ops: RDFOps[Rdf]): GraphStore[Rdf] =
    GraphStore[Rdf](store)

  implicit def RDFStore2SparqlEngine[Rdf <: RDF](store: RDFStore[Rdf]): SparqlEngine[Rdf] =
    SparqlEngine[Rdf](store)

}
