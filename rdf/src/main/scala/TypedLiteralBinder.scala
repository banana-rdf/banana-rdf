package org.w3.banana

import scalaz._

trait TypedLiteralBinder[Rdf <: RDF, T] {
  def fromTypedLiteral(node: Rdf#TypedLiteral): Validation[BananaException, T]
  def toTypedLiteral(t: T): Rdf#TypedLiteral
}

object TypedLiteralBinder {

  def toLiteralBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: TypedLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {

      def fromLiteral(literal: Rdf#Literal): Validation[BananaException, T] =
        ops.Literal.fold(literal)(
          tl => binder.fromTypedLiteral(tl),
          ll => Failure(FailedConversion(ll.toString + " is a LangLiteral, not a TypedLiteral"))
        )

      def toLiteral(t: T): Rdf#Literal = binder.toTypedLiteral(t)

    }

}
