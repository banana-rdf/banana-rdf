package org.w3.banana.binder

import org.w3.banana._

import scala.scalajs.js

//todo: why does one need this redefined here? (It does not compile if this trait is not duplicated...
trait ToLiteral[Rdf <: RDF, -T] {
  def toLiteral(t: T): Rdf#Literal
}


object ToLiteral extends ToLiteralCore {
 
  implicit def JSDateToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, js.Date] {
      import ops._
      def toLiteral(dateTime: js.Date): Rdf#Literal = {
        val isoString:String =  dateTime.toISOString() //.asInstanceOf[String]
        Literal(isoString, xsd.dateTime)
      }
    }
}