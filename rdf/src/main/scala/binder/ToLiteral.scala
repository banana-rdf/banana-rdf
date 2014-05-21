package org.w3.banana.binder

import org.w3.banana._

trait ToLiteral[Rdf <: RDF, -T] {
  def toLiteral(t: T): Rdf#Literal
}

object ToLiteral {

  implicit def LiteralToLiteral[Rdf <: RDF] = new ToLiteral[Rdf, Rdf#Literal] {
    def toLiteral(t: Rdf#Literal): Rdf#Literal = t
  }

}
