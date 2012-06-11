package org.w3.banana

import scalaz._
import scalaz.Validation._
import org.joda.time.DateTime
import NodeBinder._

object CommonBinders {

  def apply[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf]): CommonBinders[Rdf] = new CommonBinders[Rdf]()

}

class CommonBinders[Rdf <: RDF]()(implicit ops: RDFOperations[Rdf]) {

  import ops._

  private val xsd = XSDPrefix(ops)

  implicit val StringBinder: NodeBinder[Rdf, String] = new NodeBinder[Rdf, String] {

    def fromNode(node: Rdf#Node): Validation[BananaException, String] =
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.string)
            Success(lexicalForm)
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }

    def toNode(t: String): Rdf#Node = TypedLiteral(t, xsd.string)

  }


  implicit val IntBinder: NodeBinder[Rdf, Int] = new NodeBinder[Rdf, Int] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Int] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.int)
            Success(lexicalForm.toInt)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Integer but has following datatype: " + datatype))
      }
    }

    def toNode(t: Int): Rdf#Node = TypedLiteral(t.toString, xsd.int)

  }

  implicit val DoubleBinder: NodeBinder[Rdf, Double] = new NodeBinder[Rdf, Double] {

    def fromNode(node: Rdf#Node): Validation[BananaException, Double] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.double)
            Success(lexicalForm.toDouble)
          else
            Failure(FailedConversion(lexicalForm + " may be convertible to an Double but has following datatype: " + datatype))
      }
    }

    def toNode(t: Double): Rdf#Node = TypedLiteral(t.toString, xsd.double)

  }

  implicit val DateTimeBinder: NodeBinder[Rdf, DateTime] = new NodeBinder[Rdf, DateTime] {

    def fromNode(node: Rdf#Node): Validation[BananaException, DateTime] = {
      asTypedLiteral(node) flatMap {
        case TypedLiteral(lexicalForm, datatype) =>
          if (datatype == xsd.dateTime)
            try {
              Success(DateTime.parse(lexicalForm))
            } catch {
              case t => Failure(FailedConversion(node.toString + " is of type xsd:dateTime but its lexicalForm could not be parsed: " + lexicalForm))
            }
          else
            Failure(FailedConversion(lexicalForm + " has datatype " + datatype))
      }
    }

    def toNode(t: DateTime): Rdf#Node = TypedLiteral(t.toString, xsd.dateTime)

  }

}
