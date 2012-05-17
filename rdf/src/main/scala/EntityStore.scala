package org.w3.banana

trait EntityStore[Rdf <: RDF, T] {

  def get(uri: Rdf#IRI): T

  def delete(uri: Rdf#IRI): Unit

  def put(entity: T): Unit

  def append(uri: Rdf#IRI, graph: Rdf#Graph): Unit

}

object EntityStore {

  def apply[Rdf <: RDF, T](
    ops: RDFOperations[Rdf],
    store: RDFStore[Rdf],
    binder: PointedGraphBinder[Rdf, T]): EntityStore[Rdf, T] = {

    import store._
    import binder._

    new EntityStore[Rdf, T] {

      def get(uri: Rdf#IRI): T = fromPointedGraph(PointedGraph(uri, getNamedGraph(uri)))

      def delete(uri: Rdf#IRI): Unit = removeGraph(uri)

      def put(entity: T): Unit = {
        val PointedGraph(node, graph) = toPointedGraph(entity)
        def error = sys.error("please provide an iri")
        val uri = ops.Node.fold(node)(uri => uri, _ => error, _ => error)
        addNamedGraph(uri, graph)
      }

      def append(uri: Rdf#IRI, graph: Rdf#Graph): Unit = appendToNamedGraph(uri, graph)
      
    }

  }

}
