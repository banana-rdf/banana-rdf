package org.w3.banana.binder

import org.w3.banana._
import scalajs.js

trait ToLiteralJs[Rdf <: RDF, -T] {
  def toLiteral(t: T): Rdf#Literal
}

object ToLiteralJs {
 
  implicit def JSDateToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteralJs[Rdf, js.Date] {
      import ops._
      def toLiteral(dateTime: js.Date): Rdf#Literal = {
        val isoString:String = js.Dynamic.global.moment(dateTime).toISOString().asInstanceOf[String]
        Literal(isoString, xsd.dateTime)
      }
    }
}