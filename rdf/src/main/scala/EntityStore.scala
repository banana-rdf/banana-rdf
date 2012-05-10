package org.w3.banana

trait EntityGraphBinder[Rdf <: RDF, T] {
  def fromGraph(uri: Rdf#IRI, graph: Rdf#Graph): T
  def toGraph(t: T): Rdf#Graph
  def toUri(t: T): Rdf#IRI
}

trait EntityStore[Rdf <: RDF, T] {

  def get(uri: Rdf#IRI): T

  def delete(uri: Rdf#IRI): Unit

  def put(entity: T): Unit

  def append(uri: Rdf#IRI, graph: Rdf#Graph): Unit

}

object EntityStore {

  def apply[Rdf <: RDF, T](
    store: RDFStore[Rdf],
    binder: EntityGraphBinder[Rdf, T]): EntityStore[Rdf, T] = {

    import store._
    import binder._

    new EntityStore[Rdf, T] {

      def get(uri: Rdf#IRI): T = fromGraph(uri, getNamedGraph(uri))

      def delete(uri: Rdf#IRI): Unit = removeGraph(uri)

      def put(entity: T): Unit = addNamedGraph(toUri(entity), toGraph(entity))

      def append(uri: Rdf#IRI, graph: Rdf#Graph): Unit = appendToNamedGraph(uri, graph)
      
    }

  }

}
