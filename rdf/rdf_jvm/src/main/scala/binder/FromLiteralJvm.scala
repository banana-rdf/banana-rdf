package org.w3.banana.binder

import org.w3.banana._
import scala.util._
import org.joda.time.DateTime

//todo: why does one need this redefined here? (It does not compile if this trait is not duplicated...
trait FromLiteral[Rdf <: RDF, +T] {
  def fromLiteral(literal: Rdf#Literal): Try[T]
}

//todo: why does one need this redefined here? (It does not compile if this trait is not duplicated...
trait ToLiteral[Rdf <: RDF, -T] {
  def toLiteral(t: T): Rdf#Literal
}

object ToLiteral extends ToLiteralCore

object FromLiteral extends FromLiteralCore {

  implicit def DateTimeFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, DateTime] {
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