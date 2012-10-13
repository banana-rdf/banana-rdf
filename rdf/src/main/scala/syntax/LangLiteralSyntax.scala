package org.w3.banana.syntax

import org.w3.banana._

class LangLiteralSyntax[Rdf <: RDF](ll: Rdf#LangLiteral)(implicit ops: RDFOps[Rdf]) {

  def lang: Rdf#Lang = ops.fromLangLiteral(ll)._2

}
