package org.w3.banana.syntax

import org.w3.banana._

trait StringSyntax {

  implicit def stringW(s: String) = new StringW(s)

}

object StringSyntax extends StringSyntax

class StringW(val s: String) extends AnyVal {

  def lang[Rdf <: RDF](langString: String)(implicit ops: RDFOps[Rdf]): Rdf#LangLiteral = ops.makeLangLiteral(s, ops.makeLang(langString))

}
