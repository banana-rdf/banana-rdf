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

    def fromLiteral(literal: Rdf#Literal): Validation[Throwable, String] = fromTryCatch {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.string)
              lexicalForm
            else
              sys.error("asString: " + lexicalForm + " has datatype " + datatype)
        },
        langLiteral => sys.error("asString: " + langLiteral + " is a langLiteral, you want to access its lexical form")
      )
    }

    def toLiteral(t: String): Rdf#Literal = TypedLiteral(t, xsd.string)

  }


  implicit val IntBinder: LiteralBinder[Rdf, Int] = new LiteralBinder[Rdf, Int] {

    def fromLiteral(literal: Rdf#Literal): Validation[Throwable, Int] = fromTryCatch {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.integer)
              lexicalForm.toInt
            else
              sys.error("asInt: " + lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype)
        },
        langLiteral => sys.error("asInt: " + langLiteral + " is a langLiteral")
      )
    }

    def toLiteral(t: Int): Rdf#Literal = TypedLiteral(t.toString, xsd.int)

  }

  implicit val DoubleBinder: LiteralBinder[Rdf, Double] = new LiteralBinder[Rdf, Double] {

    def fromLiteral(literal: Rdf#Literal): Validation[Throwable, Double] = fromTryCatch {
      Literal.fold(literal)(
        {
          case TypedLiteral(lexicalForm, datatype) =>
            if (datatype == xsd.double)
              lexicalForm.toDouble
            else
              sys.error("asDouble: " + lexicalForm + " may be convertible to an Double but has following datatype: " + datatype)
        },
        langLiteral => sys.error("asDouble: " + langLiteral + " is a langLiteral")
      )
    }

    def toLiteral(t: Double): Rdf#Literal = TypedLiteral(t.toString, xsd.double)

  }

}
