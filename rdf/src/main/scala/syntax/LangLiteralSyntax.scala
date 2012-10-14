package org.w3.banana.syntax

import org.w3.banana._

class LangLiteralSyntax[Rdf <: RDF](val ll: Rdf#LangLiteral) extends AnyVal {

  def lang(implicit ops: RDFOps[Rdf]): Rdf#Lang = ops.fromLangLiteral(ll)._2

}
