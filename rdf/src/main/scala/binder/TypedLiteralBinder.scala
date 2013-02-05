package org.w3.banana.binder

import org.w3.banana._
import scala.util._

trait TypedLiteralBinder[Rdf <: RDF, T] extends FromTypedLiteral[Rdf, T] with ToTypedLiteral[Rdf, T]

object TypedLiteralBinder {

  implicit def FromTypedLiteralToTypedLiteral2TypedLiteralBinder[Rdf <: RDF, T](implicit from: FromTypedLiteral[Rdf, T], to: ToTypedLiteral[Rdf, T]): TypedLiteralBinder[Rdf, T] =
    new TypedLiteralBinder[Rdf, T] {
      def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[T] = from.fromTypedLiteral(tl)
      def toTypedLiteral(t: T): Rdf#TypedLiteral = to.toTypedLiteral(t)
    }

}
