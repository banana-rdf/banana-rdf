package org.w3.banana

import org.w3.banana.scalaz._

trait LiteralBinder[Rdf <: RDF, T] {
  def fromLiteral(literal: Rdf#Literal): Validation[BananaException, T]
  def toLiteral(t: T): Rdf#Literal
}

object LiteralBinder {

  implicit def toNodeBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: LiteralBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Validation[BananaException, T] =
        ops.Node.fold(node)(
          uri => Failure(FailedConversion(node + " is a URI, not a Literal")),
          bnode => Failure(FailedConversion(node + " is a BNode, not a Literal")),
          literal => binder.fromLiteral(literal)
        )

      def toNode(t: T): Rdf#Node = binder.toLiteral(t)
    }

}
