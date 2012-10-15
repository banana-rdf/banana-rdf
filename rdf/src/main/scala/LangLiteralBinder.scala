package org.w3.banana

import scala.util._

trait LangLiteralBinder[Rdf <: RDF, T] {
  def fromLangLiteral(node: Rdf#LangLiteral): Try[T]
  def toLangLiteral(t: T): Rdf#LangLiteral
}
