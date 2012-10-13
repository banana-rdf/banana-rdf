package org.w3.banana

import scala.util._
import org.joda.time.DateTime
import java.util.UUID

trait CommonBinders[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit val StringLiteralBinder: TypedLiteralBinder[Rdf, String] = new TypedLiteralBinder[Rdf, String] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[String] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.string)
        Success(lexicalForm)
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: String): Rdf#TypedLiteral = TypedLiteral(t, xsd.string)

  }

  implicit val StringStringBinder: StringBinder[String] = StringBinder { s => Success(s) }

  implicit val UUIDStringBinder: StringBinder[UUID] = StringBinder { uuid =>
    try {
      Success(UUID.fromString(uuid))
    } catch {
      case _: IllegalArgumentException => Failure(WrongExpectation(uuid + " cannot be made a UUID"))
    }
  }

  implicit val BooleanLiteralBinder: TypedLiteralBinder[Rdf, Boolean] = new TypedLiteralBinder[Rdf, Boolean] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[Boolean] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.boolean)
        lexicalForm match {
          case "true" | "1" => Success(true)
          case "false" | "0" => Success(false)
          case other => Failure(FailedConversion(other + " is not in the lexical space for xsd:boolean"))
        }
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: Boolean): Rdf#TypedLiteral = TypedLiteral(if (t) "true" else "false", xsd.boolean)

  }

  // TODO: find a better datatype than xsd:string
  implicit val UUIDBinder: TypedLiteralBinder[Rdf, UUID] = new TypedLiteralBinder[Rdf, UUID] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[UUID] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.string)
        try { Success(UUID.fromString(lexicalForm)) } catch { case _: IllegalArgumentException => Failure(FailedConversion(lexicalForm + " cannot be made a java.util.UUID")) }
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: UUID): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.string)

  }

  implicit val IntBinder: TypedLiteralBinder[Rdf, Int] = new TypedLiteralBinder[Rdf, Int] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[Int] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.integer)
        Success(lexicalForm.toInt)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Int): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.integer)

  }

  implicit val DoubleBinder: TypedLiteralBinder[Rdf, Double] = new TypedLiteralBinder[Rdf, Double] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[Double] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.double)
        Success(lexicalForm.toDouble)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Double): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.double)

  }

  implicit val DateTimeBinder: TypedLiteralBinder[Rdf, DateTime] = new TypedLiteralBinder[Rdf, DateTime] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Try[DateTime] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.dateTime)
        try {
          Success(DateTime.parse(lexicalForm))
        } catch {
          case t: Throwable => Failure(FailedConversion(literal.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
        }
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: DateTime): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.dateTime)

  }

}
