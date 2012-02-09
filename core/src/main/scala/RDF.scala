package org.w3.rdf

import org.w3.algebraic._

trait Module {
  val xsdString = IRI("http://www.w3.org/2001/XMLSchema#string")
  val xsdStringType = Right(xsdString)


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

  val Literal: LiteralDataType[String, Literal]

  val Lang: AlgebraicDataType1[String, Lang]

  //attempt to get an optional apply
  trait LiteralDataType[String, Literal] extends AlgebraicDataType2[String,  LiteralType, Literal] {
    def apply(lexicalForm: String): Literal = apply(lexicalForm, xsdStringType)
  }

  implicit def iri2Right(iri: IRI) = Right(iri)
  implicit def lang2Left(lang: Lang) = Left(lang)

}





