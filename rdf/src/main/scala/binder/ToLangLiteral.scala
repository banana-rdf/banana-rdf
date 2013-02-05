package org.w3.banana.binder

import org.w3.banana._

trait ToLangLiteral[Rdf <: RDF, -T] {
  def toLangLiteral(t: T): Rdf#LangLiteral
}

object ToLangLiteral {

  implicit def LangLiteralToLangLiteral[Rdf <: RDF] = new ToLangLiteral[Rdf, Rdf#LangLiteral] {
    def toLangLiteral(t: Rdf#LangLiteral): Rdf#LangLiteral = t
  }

}
