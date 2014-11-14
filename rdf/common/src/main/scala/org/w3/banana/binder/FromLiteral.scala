package org.w3.banana.binder

import java.math.BigInteger

import org.w3.banana._

import scala.util._

trait FromLiteral[Rdf <: RDF, +T] {
  def fromLiteral(literal: Rdf#Literal): Try[T]
}

object FromLiteral {

  implicit def LiteralFromLiteral[Rdf <: RDF] = new FromLiteral[Rdf, Rdf#Literal] {
    def fromLiteral(literal: Rdf#Literal): Success[Rdf#Literal] = Success(literal)
  }

  implicit def StringFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, String] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[String] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.string)
        Success(lexicalForm)
      else
        Failure(FailedConversion(s"${literal} is not an xsd:string"))
    }
  }

  implicit def BooleanFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, Boolean] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[Boolean] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.boolean) {
        lexicalForm match {
          case "true" | "1" => Success(true)
          case "false" | "0" => Success(false)
          case other => Failure(FailedConversion(s"${other} is not in the lexical space for xsd:boolean"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:boolean"))
      }
    }
  }

  implicit def IntFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, Int] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[Int] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.integer) {
        try {
          Success(lexicalForm.toInt)
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${literal} is an xsd.integer but is not an acceptable integer"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:int"))
      }
    }
  }

  implicit def BigIntFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, BigInteger] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[BigInteger] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.integer) {
        try {
          Success(new BigInteger(lexicalForm))
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${literal} is an xsd.integer but is not an acceptable integer"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:integer"))
      }
    }
  }

  implicit def DoubleFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, Double] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[Double] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.double) {
        try {
          Success(lexicalForm.toDouble)
        } catch {
          case _: NumberFormatException => Failure(FailedConversion(s"${literal} is an xsd.double but is not an acceptable double"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:double"))
      }
    }
  }

  /*
 
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
*/
  implicit def ByteArrayFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, Array[Byte]] {
    import ops._
    val whitespace = "\\s".r
    def hex2Bytes(hex: String): Try[Array[Byte]] = Try {
      val cleaned = whitespace.replaceAllIn(hex, "") //avoid obvious hex encoding errors ( not standard, but no other interpretation makes sense )
      val x = for { i <- 0 to hex.length - 1 by 2 }
        yield cleaned.substring(i, i + 2)
      x.map(Integer.parseInt(_, 16).toByte).toArray
    }
    def fromLiteral(literal: Rdf#Literal): Try[Array[Byte]] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.hexBinary) {
        hex2Bytes(lexicalForm) recoverWith {
          case _: NumberFormatException => Failure(FailedConversion(s"${literal} cannot be parsed as an xsd:hexBinary"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:datetime"))
      }
    }
  }

}
