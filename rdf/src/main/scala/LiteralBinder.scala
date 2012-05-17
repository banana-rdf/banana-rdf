package org.w3.banana

import scalaz._

trait LiteralBinder[Rdf <: RDF, T] {
  def fromLiteral(literal: Rdf#Literal): Validation[Throwable, T]
  def toLiteral(t: T): Rdf#Literal
}
