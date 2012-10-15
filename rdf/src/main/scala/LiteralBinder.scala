package org.w3.banana

import scala.util._

trait LiteralBinder[Rdf <: RDF, T] extends Any {
  def fromLiteral(literal: Rdf#Literal): Try[T]
  def toLiteral(t: T): Rdf#Literal
}

object LiteralBinder {

  implicit def literalBinderForTypedLiteral[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: TypedLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {

      def fromLiteral(literal: Rdf#Literal): Try[T] =
        ops.foldLiteral(literal)(
          tl => binder.fromTypedLiteral(tl),
          ll => Failure(FailedConversion(ll.toString + " is a LangLiteral, not a TypedLiteral"))
        )

      def toLiteral(t: T): Rdf#Literal = binder.toTypedLiteral(t)

    }

  implicit class LiteralBinderW[Rdf <: RDF, T](val binder: LiteralBinder[Rdf, T]) extends AnyVal {
    def toNodeBinder(implicit ops: RDFOps[Rdf]): NodeBinder[Rdf, T] = NodeBinder.NodeBinderForLiteral(ops, binder)
  }

  class LiteralBinderForLiteral[Rdf <: RDF](val ops: RDFOps[Rdf]) extends AnyVal with LiteralBinder[Rdf, Rdf#Literal] {
    def fromLiteral(literal: Rdf#Literal): Try[Rdf#Literal] = Success(literal)
    def toLiteral(t: Rdf#Literal): Rdf#Literal = t
  }
    
  def literalBinderForLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]): LiteralBinder[Rdf, Rdf#Literal] =
    new LiteralBinderForLiteral[Rdf](ops)

  implicit def literalBinderForLangLiteral[Rdf <: RDF, T](implicit ops: RDFOps[Rdf], binder: LangLiteralBinder[Rdf, T]): LiteralBinder[Rdf, T] =
    new LiteralBinder[Rdf, T] {

      def fromLiteral(literal: Rdf#Literal): Try[T] =
        ops.foldLiteral(literal)(
          tl => Failure(FailedConversion(literal.toString + " is a TypedLiteral, not a LangLiteral")),
          ll => binder.fromLangLiteral(ll)
        )

      def toLiteral(t: T): Rdf#Literal = binder.toLangLiteral(t)

    }

}
