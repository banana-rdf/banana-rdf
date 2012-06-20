package org.w3.banana

import org.w3.banana.scalaz._

trait LangLiteralBinder[Rdf <: RDF, T] {
  def fromLangLiteral(node: Rdf#LangLiteral): Validation[BananaException, T]
  def toLangLiteral(t: T): Rdf#LangLiteral
}

object LangLiteralBinder {

  def toLiteralBinder[Rdf <: RDF, T](implicit ops: RDFOperations[Rdf], binder: LangLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {

      def fromLiteral(literal: Rdf#Literal): Validation[BananaException, T] =
        ops.Literal.fold(literal)(
          tl => Failure(FailedConversion(literal.toString + " is a TypedLiteral, not a LangLiteral")),
          ll => binder.fromLangLiteral(ll)
        )

      def toLiteral(t: T): Rdf#Literal = binder.toLangLiteral(t)

    }

}
