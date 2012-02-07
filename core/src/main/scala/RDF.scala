package org.w3.rdf

import org.w3.algebraic._

trait Model {

  trait GraphInterface extends Iterable[Triple] { self =>
    def ++(other: Graph): Graph
  }
  type Graph <: GraphInterface
  type Triple
  type Node
  type NodeIRI <: Node
  type NodeBNode <: Node
  type NodeLiteral <: Node
  type IRI
  type BNode
  type Literal
  type LangTag

  trait GraphCompanionObject {
    def empty: Graph
    def apply(elems: Triple*): Graph
    def apply(it: Iterable[Triple]): Graph
  }

  val Graph: GraphCompanionObject

  val Triple: AlgebraicDataType3[Node, IRI, Node, Triple]

  val NodeIRI: AlgebraicDataType1[IRI, NodeIRI]
  val NodeBNode: AlgebraicDataType1[BNode, NodeBNode]
  val NodeLiteral: AlgebraicDataType1[Literal, NodeLiteral]
  
  val IRI : AlgebraicDataType1[String, IRI]

  val BNode: AlgebraicDataType1[String, BNode]

  val Literal: AlgebraicDataType3[String, Option[LangTag], Option[IRI], Literal]

  val LangTag: AlgebraicDataType1[String, LangTag]

}




