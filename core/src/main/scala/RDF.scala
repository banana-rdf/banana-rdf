package org.w3.rdf

import org.w3.algebraic._

trait Module {
  val xsdString = "http://www.w3.org/2001/XMLSchema#string"
  val xsdStringIRI = IRI(xsdString)
  val rdfLang = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"


  trait GraphInterface extends Iterable[Triple] { self =>
    def ++(other: Graph): Graph
  }
  type Graph <: GraphInterface
  type Triple
  type Node
  type IRI <: Node
  type BNode <: Node
  type Literal <: Node
  type Lang <: IRI

  trait GraphCompanionObject {
    def empty: Graph
    def apply(elems: Triple*): Graph
    def apply(it: Iterable[Triple]): Graph
  }

  val Graph: GraphCompanionObject

  val Triple: AlgebraicDataType3[Node, IRI, Node, Triple]

  val IRI : AlgebraicDataType1[String, IRI]

  val BNode: AlgebraicDataType1[String, BNode]

  val Literal: AlgebraicDataType2[String, IRI, Literal] with Function1[String, Literal]

  val Lang: AlgebraicDataType1[String, Lang]

}





