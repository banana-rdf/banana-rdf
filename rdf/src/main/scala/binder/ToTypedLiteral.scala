package org.w3.banana.binder

import org.w3.banana._
import org.joda.time.DateTime

trait ToTypedLiteral[Rdf <: RDF, -T] {
  def toTypedLiteral(t: T): Rdf#TypedLiteral
}

object ToTypedLiteral {

  implicit def TypedLiteralToTypedLiteral[Rdf <: RDF] = new ToTypedLiteral[Rdf, Rdf#TypedLiteral] {
    def toTypedLiteral(tl: Rdf#TypedLiteral): Rdf#TypedLiteral = tl
  }

  implicit def StringToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, String] {
      import ops._
      def toTypedLiteral(s: String): Rdf#TypedLiteral = TypedLiteral(s, xsd.string)
    }

  implicit def BooleanToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, Boolean] {
      import ops._
      def toTypedLiteral(b: Boolean): Rdf#TypedLiteral = TypedLiteral(if (b) "true" else "false", xsd.boolean)
    }

  implicit def IntToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, Int] {
      import ops._
      def toTypedLiteral(i: Int): Rdf#TypedLiteral = TypedLiteral(i.toString, xsd.integer)
    }

  implicit def DoubleToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, Double] {
      import ops._
      def toTypedLiteral(d: Double): Rdf#TypedLiteral = TypedLiteral(d.toString, xsd.double)
    }

  implicit def DateTimeToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, DateTime] {
      import ops._
      def toTypedLiteral(dateTime: DateTime): Rdf#TypedLiteral = TypedLiteral(dateTime.toString, xsd.dateTime)
    }

}
