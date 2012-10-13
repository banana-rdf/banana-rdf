package org.w3.banana.syntax

import org.w3.banana._

class LiteralSyntax[Rdf <: RDF](literal: Rdf#Literal)(implicit ops: RDFOps[Rdf]) {

  def fold[T](funTL: Rdf#TypedLiteral => T, funLL: Rdf#LangLiteral => T): T =
    ops.foldLiteral(literal)(funTL, funLL)

  def lexicalForm: String =
    ops.foldLiteral(literal)(
      tl => ops.fromTypedLiteral(tl)._1,
      ll => ops.fromLangLiteral(ll)._1
    )

}
