package org.w3.rdf

import org.w3.algebraic._

trait Module {

  val xsdStringType = Right(IRI("http://www.w3.org/2001/XMLSchema#string"))
//  val rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"


  trait GraphInterface extends Iterable[Triple] { self =>
    def ++(other: Graph): Graph
  }
  type Graph <: GraphInterface
  type Triple
  type Node
  type IRI <: Node
  type BNode <: Node
  type Literal <: Node
  type Lang
  type LiteralType = Either[Lang,IRI]

  trait GraphCompanionObject {
    def empty: Graph
    def apply(elems: Triple*): Graph
    def apply(it: Iterable[Triple]): Graph
  }

  val Graph: GraphCompanionObject

  val Triple: AlgebraicDataType3[Node, IRI, Node, Triple]

  val IRI : AlgebraicDataType1[String, IRI]

  val BNode: AlgebraicDataType1[String, BNode]

  val Literal: AlgebraicDataType2[String,LiteralType, Literal]

  val Lang: AlgebraicDataType1[String, Lang]

}




