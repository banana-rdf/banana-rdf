package org.w3.banana.binder

import org.joda.time.DateTime
import org.w3.banana._
import scala.util._

object JodaTimeBinders {

  implicit def DateTimeToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, DateTime] {
      import ops._
      def toLiteral(dateTime: DateTime): Rdf#Literal = Literal(dateTime.toString, xsd.dateTime)
    }

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
