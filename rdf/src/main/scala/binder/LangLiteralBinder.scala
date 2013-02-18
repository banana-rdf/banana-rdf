package org.w3.banana.binder

import org.w3.banana._
import scala.util._

trait LangLiteralBinder[Rdf <: RDF, T] extends FromLangLiteral[Rdf, T] with ToLangLiteral[Rdf, T]

object LangLiteralBinder {

  implicit def FromLangLiteralToLangLiteral2LangLiteralBinder[Rdf <: RDF, T](implicit from: FromLangLiteral[Rdf, T], to: ToLangLiteral[Rdf, T]): LangLiteralBinder[Rdf, T] =
    new LangLiteralBinder[Rdf, T] {
      def fromLangLiteral(ll: Rdf#LangLiteral): Try[T] = from.fromLangLiteral(ll)
      def toLangLiteral(t: T): Rdf#LangLiteral = to.toLangLiteral(t)
    }

}
