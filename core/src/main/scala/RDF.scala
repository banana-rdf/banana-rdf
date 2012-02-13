package org.w3.rdf

import org.w3.algebraic._

/**
 * A Module that gathers the types needed to define an RDF implementation
 * Closely based on
 *   http://dvcs.w3.org/hg/rdf/raw-file/default/rdf-concepts/index.html
 * But with two notable exceptions:
 *   - we allow literals in subject position - for help later in reasoning.
 *   - We make a Lang <: IRI as this massively simplifies the model whilst making it type safe.
 */
trait Module {
  lazy val xsdString = "http://www.w3.org/2001/XMLSchema#string"
  lazy val xsdStringIRI = IRI(xsdString)
  lazy val rdfLang = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"


  trait GraphInterface extends Iterable[Triple] { self =>
    def ++(other: Graph): Graph
  }
  type Graph <: GraphInterface
  type Triple
  type Node
  type IRI <: Node
  type BNode <: Node
  type Literal <: Node
  type TypedLiteral <: Literal
  type LangLiteral <: Literal
  type Lang

  trait GraphCompanionObject {
    def empty: Graph
    def apply(elems: Triple*): Graph
    def apply(it: Iterable[Triple]): Graph
  }

  val Graph: GraphCompanionObject

  val Triple: AlgebraicDataType3[Node, IRI, Node, Triple]

  trait NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T
  }
  
  val Node: NodeCompanionObject
  
  val IRI : AlgebraicDataType1[String, IRI]

  val BNode: AlgebraicDataType1[String, BNode]

  trait LiteralCompanionObject {
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T
  }
  
  val Literal: LiteralCompanionObject
  
  trait TypedLiteralCompanionObject extends AlgebraicDataType2[String, IRI, TypedLiteral] with Function1[String, TypedLiteral] {
    def apply(lexicalForm: String): TypedLiteral = TypedLiteral(lexicalForm, xsdStringIRI)
  }
  
  val TypedLiteral: TypedLiteralCompanionObject
  
  val LangLiteral: AlgebraicDataType2[String, Lang, LangLiteral]
  
  val Lang: AlgebraicDataType1[String, Lang]

}





