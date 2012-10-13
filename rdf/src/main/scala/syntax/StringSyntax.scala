package org.w3.banana.syntax

import org.w3.banana._

class StringSyntax(s: String) {

  def lang[Rdf <: RDF](langString: String)(implicit ops: RDFOps[Rdf]): Rdf#LangLiteral = ops.makeLangLiteral(s, ops.makeLang(langString))

}
