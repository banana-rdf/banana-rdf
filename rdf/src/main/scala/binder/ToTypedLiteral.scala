package org.w3.banana.binder

import org.w3.banana._
import org.joda.time.DateTime
import util.{Failure, Try}
import java.math.BigInteger

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
      def toTypedLiteral(i: Int): Rdf#TypedLiteral = TypedLiteral(i.toString, xsd.int)
    }

  implicit def BigIntToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, BigInteger] {
      import ops._
      def toTypedLiteral(i: BigInteger): Rdf#TypedLiteral = TypedLiteral(i.toString, xsd.integer)
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


  implicit def ByteArrayToTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToTypedLiteral[Rdf, Array[Byte]] {
    import ops._
      def bytes2Hex( bytes: Array[Byte] ): String = {
        def cvtByte( b: Byte ): String = {
          val c = b & 0xff
          (if ( c < 0x10 ) "0" else "" ) + java.lang.Long.toString( c & 0xff, 16 )
        }
        bytes.map( cvtByte( _ )).mkString
      }
    def toTypedLiteral(bytes: Array[Byte]): Rdf#TypedLiteral = {
      TypedLiteral(bytes2Hex(bytes),xsd.hexBinary)
    }
  }



}
