package org.w3.banana

import scalaz._

trait NodeBinder[Rdf <: RDF, T] {
  def fromNode(node: Rdf#Node): Validation[BananaException, T]
  def toNode(t: T): Rdf#Node
}

object NodeBinder {

  def toPointedGraphBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Validation[BananaException, T] =
        binder.fromNode(pointed.node)

      def toPointedGraph(t: T): PointedGraph[Rdf] = PointedGraph(binder.toNode(t))
    }

}
