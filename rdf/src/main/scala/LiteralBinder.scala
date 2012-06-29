package org.w3.banana

import scalaz._

trait LiteralBinder[Rdf <: RDF, T] {
  def fromLiteral(literal: Rdf#Literal): Validation[BananaException, T]
  def toLiteral(t: T): Rdf#Literal
}

object LiteralBinder {

  implicit def toNodeBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: LiteralBinder[Rdf, T]): NodeBinder[Rdf, T] =
    new NodeBinder[Rdf, T] {

      def fromNode(node: Rdf#Node): Validation[BananaException, T] =
        ops.foldNode(node)(
          uri => Failure(FailedConversion(node + " is a URI, not a Literal")),
          bnode => Failure(FailedConversion(node + " is a BNode, not a Literal")),
          literal => binder.fromLiteral(literal)
        )

      def toNode(t: T): Rdf#Node = binder.toLiteral(t)
    }

  def naturalBinder[Rdf <: RDF](implicit ops: RDFOperations[Rdf]): LiteralBinder[Rdf, Rdf#Literal] =
    new LiteralBinder[Rdf, Rdf#Literal] {

      def fromLiteral(literal: Rdf#Literal): Validation[BananaException, Rdf#Literal] =
        Success(literal)

      def toLiteral(t: Rdf#Literal): Rdf#Literal = t

    }

}
