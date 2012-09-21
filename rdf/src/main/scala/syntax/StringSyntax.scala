package org.w3.banana.syntax

import org.w3.banana._

trait StringSyntax[Rdf <: RDF] {
  this: RDFOpsSyntax[Rdf] =>

  implicit def stringWrapper(s: String): StringW = new StringW(s)

  class StringW(s: String) {

    def lang(langString: String): Rdf#LangLiteral = ops.makeLangLiteral(s, ops.makeLang(langString))

  }

}
