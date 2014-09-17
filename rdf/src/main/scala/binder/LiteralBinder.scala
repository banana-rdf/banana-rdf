package org.w3.banana.binder

import org.w3.banana._

import scala.util._

trait LiteralBinder[Rdf <: RDF, T] extends FromLiteral[Rdf, T] with ToLiteral[Rdf, T]

object LiteralBinder {

  implicit def FromLiteralToLiteral2LiteralBinder[Rdf <: RDF, T](implicit from: FromLiteral[Rdf, T], to: ToLiteral[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {
      def fromLiteral(literal: Rdf#Literal): Try[T] = from.fromLiteral(literal)
      def toLiteral(t: T): Rdf#Literal = to.toLiteral(t)
    }

}
