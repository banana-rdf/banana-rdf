package org.w3.banana.binder

import org.w3.banana._

import scala.util._

trait FromNode[Rdf <: RDF, +T] {
  def fromNode(node: Rdf#Node): Try[T]
}

object FromNode {

  implicit def PointedGraphFromNode[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromNode[Rdf, PointedGraph[Rdf]] {
    def fromNode(node: Rdf#Node): Try[PointedGraph[Rdf]] = Success(PointedGraph(node))
  }

  implicit def NodeFromNode[Rdf <: RDF] = new FromNode[Rdf, Rdf#Node] {
    def fromNode(node: Rdf#Node): Try[Rdf#Node] = Success(node)
  }

  implicit def FromURIFromNode[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], from: FromURI[Rdf, T]) =
    new FromNode[Rdf, T] {
      def fromNode(node: Rdf#Node): Try[T] = ops.foldNode(node)(
        uri => from.fromURI(uri),
        bnode => Failure(FailedConversion(s"expected URI, got BNode: $bnode")),
        literal => Failure(FailedConversion(s"expected URI, got Literal: $literal"))
      )
    }

  implicit def FromLiteralFromNode[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], from: FromLiteral[Rdf, T]) = new FromNode[Rdf, T] {
    def fromNode(node: Rdf#Node): Try[T] = ops.foldNode(node)(
      uri => Failure(FailedConversion(s"expected Literal, got URI: $uri")),
      bnode => Failure(FailedConversion(s"expected Literal, got BNode: $bnode")),
      literal => from.fromLiteral(literal)
    )
  }

}
