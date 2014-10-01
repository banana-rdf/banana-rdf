package org.w3.banana.binder

import org.w3.banana._
import scala.scalajs.js
import scala.util._

object JsDateBinders {

  implicit def JSDateToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, js.Date] {
      import ops._
      def toLiteral(dateTime: js.Date): Rdf#Literal = {
        val isoString: String = dateTime.toISOString() //.asInstanceOf[String]
        Literal(isoString, xsd.dateTime)
      }
    }

  implicit def JSDateFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, js.Date] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[js.Date] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.dateTime) {
        try {
          val parsed: js.Date = new js.Date(lexicalForm)
          Success(parsed)
        } catch {
          case _: IllegalArgumentException => Failure(FailedConversion(s"${literal} is an xsd.datetime but is not an acceptable js Date"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:datetime"))
      }
    }
  }

}
