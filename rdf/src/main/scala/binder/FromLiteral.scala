package org.w3.banana.binder

import org.w3.banana._
import scala.util._

trait FromLiteral[Rdf <: RDF, +T] {
  def fromLiteral(literal: Rdf#Literal): Try[T]
}

object FromLiteral {

  implicit def LiteralFromLiteral[Rdf <: RDF] = new FromLiteral[Rdf, Rdf#Literal] {
    def fromLiteral(literal: Rdf#Literal): Success[Rdf#Literal] = Success(literal)
  }

}
