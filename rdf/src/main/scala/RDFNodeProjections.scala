package org.w3.banana

import scalaz._
import scalaz.Validation._

trait RDFNodeProjections[Rdf <: RDF] { self =>

  def asString(node: Rdf#Node): Validation[Throwable, String]

  def asInt(node: Rdf#Node): Validation[Throwable, Int]

  def asDouble(node: Rdf#Node): Validation[Throwable, Double]

  class RDFNodeProjectionsW(node: Rdf#Node) {
    def asString: Validation[Throwable, String] = self.asString(node)
    def asInt: Validation[Throwable, Int] = self.asInt(node)
    def asDouble: Validation[Throwable, Double] = self.asDouble(node)
  }

  implicit def rdfNode2RDFNodeProjectionsW(node: Rdf#Node): RDFNodeProjectionsW = new RDFNodeProjectionsW(node)

}



object RDFNodeProjections {

  def apply[Rdf <: RDF](ops: RDFOperations[Rdf]): RDFNodeProjections[Rdf] =
    new RDFNodeProjectionsBuilder(ops)

}


class RDFNodeProjectionsBuilder[Rdf <: RDF](ops: RDFOperations[Rdf]) extends RDFNodeProjections[Rdf] {

  import ops._

  val xsd = XSDPrefix(ops)

  def asLiteral(node: Rdf#Node): Rdf#Literal =
    Node.fold(node)(
      iri => sys.error("asLiteral: " + node.toString + " is not a literal"),
      bnode => sys.error("asLiteral: " + node.toString + " is not a literal"),
      literal => literal
    )

  def asString(node: Rdf#Node): Validation[Throwable, String] = fromTryCatch {
    Literal.fold(asLiteral(node))(
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

  def asInt(node: Rdf#Node): Validation[Throwable, Int] = fromTryCatch {
    Literal.fold(asLiteral(node))(
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

  def asDouble(node: Rdf#Node): Validation[Throwable, Double] = fromTryCatch {
    Literal.fold(asLiteral(node))(
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

}
