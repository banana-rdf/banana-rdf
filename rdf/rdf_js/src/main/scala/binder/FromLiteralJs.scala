package org.w3.banana.binder

import org.w3.banana._
import scala.util._
import scalajs.js


//todo: why does one need this redefined here? (It does not compile if this trait is not duplicated...
trait FromLiteral[Rdf <: RDF, +T] {
  def fromLiteral(literal: Rdf#Literal): Try[T]
}



object FromLiteral extends FromLiteralCore {

  implicit def JSDateFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, js.Date] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[js.Date] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.dateTime) {
        try {
          val parsed:js.Date = js.Dynamic.global.moment(lexicalForm).toDate().asInstanceOf[js.Date]
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