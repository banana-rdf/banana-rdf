package org.w3.banana

import scalaz._
import scalaz.Validation._

object CommonLiteralBinders {

  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): CommonLiteralBinders[Rdf] = new CommonLiteralBinders[Rdf](ops)

}

class CommonLiteralBinders[Rdf <: RDF](ops: RDFOperations[Rdf]) {

  import ops._

  private val xsd = XSDPrefix(ops)

  implicit val StringBinder: LiteralBinder[Rdf, String] = new LiteralBinder[Rdf, String] {

    def fromLiteral(literal: Rdf#Literal): Validation[BananaException, String] = {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.string)
              Success(lexicalForm)
            else
              Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
        },
        langLiteral => Failure(FailedConversion(langLiteral + " is a langLiteral, you want to access its lexical form"))
      )
    }

    def toLiteral(t: String): Rdf#Literal = TypedLiteral(t, xsd.string)

  }


  implicit val IntBinder: LiteralBinder[Rdf, Int] = new LiteralBinder[Rdf, Int] {

    def fromLiteral(literal: Rdf#Literal): Validation[BananaException, Int] = {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.integer)
              Success(lexicalForm.toInt)
            else
              Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
        },
        langLiteral => Failure(FailedConversion(langLiteral + " is a langLiteral"))
      )
    }

    def toLiteral(t: Int): Rdf#Literal = TypedLiteral(t.toString, xsd.int)

  }

  implicit val DoubleBinder: LiteralBinder[Rdf, Double] = new LiteralBinder[Rdf, Double] {

    def fromLiteral(literal: Rdf#Literal): Validation[BananaException, Double] = {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.double)
              Success(lexicalForm.toDouble)
            else
              Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
        },
        langLiteral => Failure(FailedConversion(langLiteral + " is a langLiteral"))
      )
    }

    def toLiteral(t: Double): Rdf#Literal = TypedLiteral(t.toString, xsd.double)

  }

}
