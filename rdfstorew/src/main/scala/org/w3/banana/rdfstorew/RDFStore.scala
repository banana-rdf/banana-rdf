package org.w3.banana.rdfstorew

import org.w3.banana._

import scala.scalajs.js

sealed trait JsNodeMatch

case class PlainNode(node: RDFStore#Node) extends JsNodeMatch

case object JsANY extends JsNodeMatch

trait SPARQLSolution {

}

class SPARQLSolutionTuple(obj: js.Dictionary[js.Any]) extends SPARQLSolution {
  val varNames: Array[String] = {
    var names: List[String] = List[String]()
    for (prop <- js.Object.keys(obj)) {
      names = names.::(prop)
    }
    names.toArray[String]
  }

  def apply(s: String) = obj.get(s)
}

trait RDFStore extends RDF {
  // types related to the RDF datamodel
  type Graph = RDFStoreGraph
  type Triple = RDFStoreTriple
  type Node = RDFStoreRDFNode
  type URI = RDFStoreNamedNode
  type BNode = RDFStoreBlankNode
  type Literal = RDFStoreLiteral
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = JsNodeMatch
  type NodeAny = JsANY.type
  type NodeConcrete = JsNodeMatch

  // types related to Sparql
  type Query = String
  type SelectQuery = String
  type ConstructQuery = String
  type AskQuery = String
  type UpdateQuery = String

  type Solution = SPARQLSolution

  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Array[SPARQLSolution]
}

/*
trait AdditionalBindings {

  implicit def DateTimeFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, js.Date] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[js.Date] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.dateTime) {
        try {
          Success(new js.Date(lexicalForm))
        } catch {
          case _: IllegalArgumentException => Failure(FailedConversion(s"${literal} is an xsd.datetime but is not an acceptable datetime"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:datetime"))
      }
    }
  }

}
*/

object FromLiteralJS {
  /*
  implicit def JSDateFromLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) = new FromLiteral[Rdf, js.Date] {
    import ops._
    def fromLiteral(literal: Rdf#Literal): Try[js.Date] = {
      val Literal(lexicalForm, datatype, _) = literal
      if (datatype == xsd.dateTime) {
        try {
          Success(new js.Date(lexicalForm))
        } catch {
          case _: IllegalArgumentException => Failure(FailedConversion(s"${literal} is an xsd.datetime but is not an acceptable js Date"))
        }
      } else {
        Failure(FailedConversion(s"${literal} is not an xsd:datetime"))
      }
    }
  }


  implicit def JSDateToLiteral[Rdf <: RDF](implicit ops: RDFOps[Rdf]) =
    new ToLiteral[Rdf, js.Date] {
      import ops._
      def toLiteral(dateTime: js.Date): Rdf#Literal = Literal(dateTime.toString, xsd.dateTime)
    }
*/
}

object RDFStore extends RDFStoreModule
