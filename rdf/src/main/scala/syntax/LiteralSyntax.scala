package org.w3.banana.syntax

import org.w3.banana._

trait LiteralSyntax[Rdf <: RDF] {

  def ops: RDFOps[Rdf]

  implicit def literalWrapper(literal: Rdf#Literal): LiteralW = new LiteralW(literal)

  class LiteralW(literal: Rdf#Literal) {

    def fold[T](funTL: Rdf#TypedLiteral => T, funLL: Rdf#LangLiteral => T): T =
      ops.foldLiteral(literal)(funTL, funLL)

    def lexicalForm: String =
      ops.foldLiteral(literal)(
        tl => ops.fromTypedLiteral(tl)._1,
        ll => ops.fromLangLiteral(ll)._1
      )

  }

}
