package org.w3.banana.binder

import org.w3.banana._

trait ToLiteral[Rdf <: RDF, -T] {
  def toLiteral(t: T): Rdf#Literal
}

object ToLiteral {

  implicit def LiteralToLiteral[Rdf <: RDF] = new ToLiteral[Rdf, Rdf#Literal] {
    def toLiteral(t: Rdf#Literal): Rdf#Literal = t
  }

  implicit def StringToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, String] {
      import ops._
      def toLiteral(s: String): Rdf#Literal = Literal(s, xsd.string)
    }

  implicit def BooleanToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, Boolean] {
      import ops._
      def toLiteral(b: Boolean): Rdf#Literal = Literal(if (b) "true" else "false", xsd.boolean)
    }

  implicit def IntToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, Int] {
      import ops._
      def toLiteral(i: Int): Rdf#Literal = Literal(i.toString, xsd.integer)
    }

  import java.math.BigInteger

  implicit def BigIntToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, BigInteger] {
      import ops._
      def toLiteral(i: BigInteger): Rdf#Literal = Literal(i.toString, xsd.integer)
    }

  implicit def DoubleToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, Double] {
      import ops._
      def toLiteral(d: Double): Rdf#Literal = Literal(d.toString, xsd.double)
    }

  /* @InTheNow will find a better way to do this
  import scalajs.js
  implicit def JSDateToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, js.Date] {
      import ops._
      def toLiteral(dateTime: js.Date): Rdf#Literal = {
        val isoString:String = js.Dynamic.global.moment(dateTime).toISOString().asInstanceOf[String]
        Literal(isoString, xsd.dateTime)
      }
    }

 import org.joda.time.DateTime

  implicit def DateTimeToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, DateTime] {
      import ops._
      def toLiteral(dateTime: DateTime): Rdf#Literal = Literal(dateTime.toString, xsd.dateTime)
    }
  */

  implicit def ByteArrayToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, Array[Byte]] {
      import ops._
      def bytes2Hex(bytes: Array[Byte]): String = {
        def cvtByte(b: Byte): String = {
          val c = b & 0xff
          (if (c < 0x10) "0" else "") + java.lang.Long.toString(c & 0xff, 16)
        }
        bytes.map(cvtByte(_)).mkString
      }
      def toLiteral(bytes: Array[Byte]): Rdf#Literal = {
        Literal(bytes2Hex(bytes), xsd.hexBinary)
      }
    }

}
