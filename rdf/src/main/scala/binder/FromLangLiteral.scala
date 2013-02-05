package org.w3.banana.binder

import org.w3.banana._
import scala.util._

trait FromLangLiteral[Rdf <: RDF, +T] {
  def fromLangLiteral(tl: Rdf#LangLiteral): Try[T]
}

object FromLangLiteral {

  implicit def LangLiteralFromLangLiteral[Rdf <: RDF] = new FromLangLiteral[Rdf, Rdf#LangLiteral] {
    def fromLangLiteral(ll: Rdf#LangLiteral): Try[Rdf#LangLiteral] = Success(ll)
  }

}
