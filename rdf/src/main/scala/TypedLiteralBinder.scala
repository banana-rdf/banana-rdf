package org.w3.banana

import scala.util._

trait TypedLiteralBinder[Rdf <: RDF, T] {
  def fromTypedLiteral(node: Rdf#TypedLiteral): Try[T]
  def toTypedLiteral(t: T): Rdf#TypedLiteral
}
