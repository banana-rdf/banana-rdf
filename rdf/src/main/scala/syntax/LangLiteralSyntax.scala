package org.w3.banana.syntax

import org.w3.banana._

trait LangLiteralSyntax {

  implicit def langLiteralW[Rdf <: RDF](ll: Rdf#LangLiteral) =
    new LangLiteralW[Rdf](ll)

}

object LangLiteralSyntax extends LangLiteralSyntax

class LangLiteralW[Rdf <: RDF](val ll: Rdf#LangLiteral) extends AnyVal {

  def lang(implicit ops: RDFOps[Rdf]): Rdf#Lang = ops.fromLangLiteral(ll)._2

}
