package org.w3.banana

import scala.util._

trait NodeBinder[Rdf <: RDF, T] extends Any {
  def fromNode(node: Rdf#Node): Try[T]
  def toNode(t: T): Rdf#Node
}

object NodeBinder {

  implicit class NodeBinderW[Rdf <: RDF, T](val nodeBinder: NodeBinder[Rdf, T]) extends AnyVal {
    def toPGB(implicit ops: RDFOps[Rdf]): PointedGraphBinder[Rdf, T] = toPointedGraphBinder(ops, nodeBinder)
  }

  def toPointedGraphBinder[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: NodeBinder[Rdf, T]): PointedGraphBinder[Rdf, T] =
    new PointedGraphBinder[Rdf, T] {

      def fromPointedGraph(pointed: PointedGraph[Rdf]): Try[T] =
        binder.fromNode(pointed.pointer)

      def toPointedGraph(t: T): PointedGraph[Rdf] = PointedGraph(binder.toNode(t))
    }

  class NodeBinderForNode[Rdf <: RDF](val ops: RDFOps[Rdf]) extends AnyVal with NodeBinder[Rdf, Rdf#Node] {
    def fromNode(node: Rdf#Node): Try[Rdf#Node] = Success(node)
    def toNode(t: Rdf#Node): Rdf#Node = t
  }

  implicit def nodeBinderForNode[Rdf <: RDF](implicit ops: RDFOps[Rdf]): NodeBinder[Rdf, Rdf#Node] =
    new NodeBinderForNode[Rdf](ops)

  implicit def NodeBinderForURI[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: URIBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Try[T] =
        ops.foldNode(node)(
          uri => binder.fromUri(uri),
          bnode => Failure(FailedConversion(node + " is a BNode, not a URI")),
          uri => Failure(FailedConversion(node + " is a Literal, not a URI"))
        )

      def toNode(t: T): Rdf#Node = binder.toUri(t)
    }

  implicit def NodeBinderForLiteral[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: LiteralBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Try[T] =
        ops.foldNode(node)(
          uri => Failure(FailedConversion(node + " is a URI, not a Literal")),
          bnode => Failure(FailedConversion(node + " is a BNode, not a Literal")),
          literal => binder.fromLiteral(literal)
        )

      def toNode(t: T): Rdf#Node = binder.toLiteral(t)
    }


}
