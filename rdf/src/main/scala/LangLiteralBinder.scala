package org.w3.banana

import scalaz._

trait LangLiteralBinder[Rdf <: RDF, T] {
  def fromLangLiteral(node: Rdf#LangLiteral): BananaValidation[T]
  def toLangLiteral(t: T): Rdf#LangLiteral
}

object LangLiteralBinder {

  def toLiteralBinder[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: LangLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {

      def fromLiteral(literal: Rdf#Literal): BananaValidation[T] =
        ops.foldLiteral(literal)(
          tl => Failure(FailedConversion(literal.toString + " is a TypedLiteral, not a LangLiteral")),
          ll => binder.fromLangLiteral(ll)
        )

      def toLiteral(t: T): Rdf#Literal = binder.toLangLiteral(t)

    }

}
