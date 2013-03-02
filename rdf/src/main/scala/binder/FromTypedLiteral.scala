package org.w3.banana.binder

import org.w3.banana._
import scala.util._
import org.joda.time.DateTime
import java.math.BigInteger

trait FromTypedLiteral[Rdf <: RDF, +T] {
  def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[T]
}

object FromTypedLiteral {

  implicit def TypedLiteralFromTypedLiteral[Rdf <: RDF] = new FromTypedLiteral[Rdf, Rdf#TypedLiteral] {
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[Rdf#TypedLiteral] = Success(tl)
  }

  implicit def StringFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, String] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[String] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.string)
        Success(lexicalForm)
      else
        Failure(FailedConversion(s"${tl} is not an xsd:string"))
    }
  }

  implicit def BooleanFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, Boolean] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[Boolean] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.boolean) {
        lexicalForm match {
          case "true" | "1" => Success(true)
          case "false" | "0" => Success(false)
          case other => Failure(FailedConversion(s"${other} is not in the lexical space for xsd:boolean"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:boolean"))
      }
    }
  }

  implicit def IntFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, Int] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[Int] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.int) {
        try {
          Success(lexicalForm.toInt)
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${tl} is an xsd.integer but is not an acceptable integer"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:int"))
      }
    }
  }

  implicit def BigIntFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, BigInteger] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[BigInteger] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.integer) {
        try {
          Success(new BigInteger(lexicalForm))
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${tl} is an xsd.integer but is not an acceptable integer"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:integer"))
      }
    }
  }


  implicit def DoubleFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, Double] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[Double] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.double) {
        try {
          Success(lexicalForm.toDouble)
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${tl} is an xsd.double but is not an acceptable double"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:double"))
      }
    }
  }

  implicit def DateTimeFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, DateTime] {
    import ops._
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[DateTime] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.dateTime) {
        try {
          Success(DateTime.parse(lexicalForm))
        } catch {
          case _: IllegalArgumentException => Failure(FailedConversion(s"${tl} is an xsd.datetime but is not an acceptable datetime"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:datetime"))
      }
    }
  }

  implicit def ByteArrayFromTypedLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromTypedLiteral[Rdf, Array[Byte]] {
    import ops._
    val whitespace = "\\s".r
    def hex2Bytes( hex: String ): Try[Array[Byte]] = Try {
      val cleaned = whitespace.replaceAllIn(hex,"") //avoid obvious hex encoding errors ( not standard, but no other interpretation makes sense )
      val x = for { i <- 0 to hex.length-1 by 2 }
              yield cleaned.substring( i, i+2 )
      x.map( Integer.parseInt( _, 16 ).toByte ).toArray
    }
    def fromTypedLiteral(tl: Rdf#TypedLiteral): Try[Array[Byte]] = {
      val TypedLiteral(lexicalForm, datatype) = tl
      if (datatype == xsd.hexBinary) {
        hex2Bytes(lexicalForm) recoverWith {
          case _: NumberFormatException => Failure(FailedConversion(s"${tl} cannot be parsed as an xsd:hexBinary"))
        }
      } else {
        Failure(FailedConversion(s"${tl} is not an xsd:datetime"))
      }
    }
  }


}
