package org.w3.banana

import akka.dispatch._

trait AsyncEntityStore[Rdf <: RDF, T] {

  def get(uri: Rdf#IRI): Future[T]

  def delete(uri: Rdf#IRI): Future[Unit]

  def put(entity: T): Future[Unit]

  def append(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit]

}

object AsyncEntityStore {

  def apply[Rdf <: RDF, T](
    ops: RDFOperations[Rdf],
    store: RDFStore[Rdf],
    binder: PointedGraphBinder[Rdf, T]): AsyncEntityStore[Rdf, T] = {

    val entityStore = EntityStore(ops, store, binder)

    new AsyncEntityStore[Rdf, T] {
      
      def get(uri: Rdf#IRI): Future[T] = null
      
      def delete(uri: Rdf#IRI): Future[Unit] = null
      
      def put(entity: T): Future[Unit] = null
      
      def append(uri: Rdf#IRI, graph: Rdf#Graph): Future[Unit] = null

    }
  }

}
