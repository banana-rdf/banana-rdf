package org.w3.banana.syntax

import org.w3.banana._

trait LangLiteralSyntax[Rdf <: RDF] {

  def ops: RDFOps[Rdf]

  implicit def typedLiteralWrapper(ll: Rdf#LangLiteral): LangLiteralW = new LangLiteralW(ll)

  class LangLiteralW(ll: Rdf#LangLiteral) {

    def lang: Rdf#Lang = ops.fromLangLiteral(ll)._2

  }

}
