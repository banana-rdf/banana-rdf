package org.w3.banana

import scalaz._
import scalaz.Validation._
import org.joda.time.DateTime

trait CommonBinders[Rdf <: RDF] {
  this: Diesel[Rdf] =>

  import ops._

  implicit val StringBinder: TypedLiteralBinder[Rdf, String] = new TypedLiteralBinder[Rdf, String] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, String] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.string)
        Success(lexicalForm)
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: String): Rdf#TypedLiteral = TypedLiteral(t, xsd.string)

  }

  implicit val IntBinder: TypedLiteralBinder[Rdf, Int] = new TypedLiteralBinder[Rdf, Int] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, Int] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.integer)
        Success(lexicalForm.toInt)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Int): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.integer)

  }

  implicit val DoubleBinder: TypedLiteralBinder[Rdf, Double] = new TypedLiteralBinder[Rdf, Double] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, Double] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.double)
        Success(lexicalForm.toDouble)
      else
        Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
    }

    def toTypedLiteral(t: Double): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.double)

  }

  implicit val DateTimeBinder: TypedLiteralBinder[Rdf, DateTime] = new TypedLiteralBinder[Rdf, DateTime] {

    def fromTypedLiteral(literal: Rdf#TypedLiteral): Validation[BananaException, DateTime] = {
      val TypedLiteral(lexicalForm, datatype) = literal
      if (datatype == xsd.dateTime)
        try {
          Success(DateTime.parse(lexicalForm))
        } catch {
          case t => Failure(FailedConversion(literal.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
        }
      else
        Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
    }

    def toTypedLiteral(t: DateTime): Rdf#TypedLiteral = TypedLiteral(t.toString, xsd.dateTime)

  }

}
