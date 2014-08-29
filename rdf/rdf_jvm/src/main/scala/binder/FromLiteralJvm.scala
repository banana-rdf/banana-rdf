package org.w3.banana.binder

import org.w3.banana._
import scala.util._
import org.joda.time.DateTime


trait FromLiteralJvm[Rdf <: RDF, +T] {
  def fromLiteral(literal: Rdf#Literal): Try[T]
}

object FromLiteralJvm {

  implicit def DateTimeFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteralJvm[Rdf, DateTime] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[DateTime] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.dateTime) {
        try {
          Success(DateTime.parse(lexicalForm))
        } catch {
          case _: IllegalArgumentException => Failure(FailedConversion(s"${literal} is an xsd.datetime but is not an acceptable datetime"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:datetime"))
      }
    }
  }
}