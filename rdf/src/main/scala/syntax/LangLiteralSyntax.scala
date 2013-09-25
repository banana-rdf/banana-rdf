package org.w3.banana.syntax

import org.w3.banana._

trait LangLiteralSyntax[Rdf <: RDF] { self: Syntax[Rdf] =>

  implicit def langLiteralW(ll: Rdf#LangLiteral) =
    new LangLiteralW[Rdf](ll)

}

class LangLiteralW[Rdf <: RDF](val ll: Rdf#LangLiteral) extends AnyVal {

  def lang(implicit ops: RDFOps[Rdf]): Rdf#Lang = ops.fromLangLiteral(ll)._2

}
