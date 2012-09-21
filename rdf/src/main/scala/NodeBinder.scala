package org.w3.banana

import scalaz._

trait NodeBinder[Rdf <: RDF, T] {
  def fromNode(node: Rdf#Node): BananaValidation[T]
  def toNode(t: T): Rdf#Node
}

object NodeBinder {

  def toPointedGraphBinder[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {

      def fromPointedGraph(pointed: PointedGraph[Rdf]): BananaValidation[T] =
        binder.fromNode(pointed.pointer)

      def toPointedGraph(t: T): PointedGraph[Rdf] = PointedGraph(binder.toNode(t))
    }

  def naturalBinder[Rdf <: RDF](implicit ops: RDFOps[Rdf]): NodeBinder[Rdf, Rdf#Node] =
    new NodeBinder[Rdf, Rdf#Node] {

      def fromNode(node: Rdf#Node): BananaValidation[Rdf#Node] =
        Success(node)

      def toNode(t: Rdf#Node): Rdf#Node = t

    }

}
