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

  trait TripleCompanionObject extends AlgebraicDataType3[Node, IRI, Node, Triple]
  
  val Triple: TripleCompanionObject

  trait NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T
  }
  
  val Node: NodeCompanionObject
  
  trait IRICompanionObject extends AlgebraicDataType1[String, IRI]
  
  val IRI : IRICompanionObject

  trait BNodeCompanionObject extends AlgebraicDataType1[String, BNode]
  
  val BNode: BNodeCompanionObject

  trait LiteralCompanionObject {
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T
  }
  
  val Literal: LiteralCompanionObject
  
  trait TypedLiteralCompanionObject extends AlgebraicDataType2[String, IRI, TypedLiteral] with Function1[String, TypedLiteral] {
    def apply(lexicalForm: String): TypedLiteral = TypedLiteral(lexicalForm, IRI("http://www.w3.org/2001/XMLSchema#string"))
  }
  
  val TypedLiteral: TypedLiteralCompanionObject
  
  trait LangLiteralCompanionObject extends AlgebraicDataType2[String, Lang, LangLiteral]
  
  val LangLiteral: LangLiteralCompanionObject
  
  trait LangCompanionObject extends AlgebraicDataType1[String, Lang]
  
  val Lang: AlgebraicDataType1[String, Lang]

  def prefixBuilder(prefix: String)(value: String): IRI = IRI(prefix+value)

  // helpers methods
  
  val xsd = prefixBuilder("http://www.w3.org/2001/XMLSchema#") _
  val rdf = prefixBuilder("http://www.w3.org/1999/02/22-rdf-syntax-ns#") _

  val xsdString = xsd("string")
  val xsdInt = xsd("int")
  val rdfLang = rdf("langString")
  
  // pimps
  
  implicit def tupleToTriple(tuple: (Node, IRI, Node)): Triple = Triple(tuple._1, tuple._2, tuple._3)
  
  class TripleW(triple: Triple) {
    val Triple(subject, predicate, objectt) = triple
  }
  
  implicit def wrapTriple(triple: Triple): TripleW = new TripleW(triple)
  
  class NodeW(node: Node) {
    def fold[T](funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T =
      Node.fold(node)(funIRI, funBNode, funLiteral)
  }
  
  implicit def wrapNode(node: Node): NodeW = new NodeW(node)
  
  class LiteralW(literal: Literal) {
    def lexicalForm = Literal.fold(literal) (
      { case TypedLiteral(s, _) => s },
      { case LangLiteral(s, _) => s }
    )
    def fold[T](funTL: TypedLiteral => T, funLL: LangLiteral => T): T = Literal.fold(literal)(funTL, funLL)
  }
  
  implicit def wrapLiteral(literal: Literal): LiteralW = new LiteralW(literal)
  
  implicit def wrapIntAsLiteral(i: Int): TypedLiteral = TypedLiteral(i.toString, xsdInt)
  
  implicit def wrapStringAsLiteral(s: String): TypedLiteral = TypedLiteral(s, xsdString)
  
  class LiteralBuilder(lexicalForm: String) {
    def ^^(datatype: IRI): TypedLiteral = TypedLiteral(lexicalForm, datatype)
    def lang(tag: String): LangLiteral = LangLiteral(lexicalForm, Lang(tag))
    def typedLiteral: TypedLiteral = TypedLiteral(lexicalForm, xsdString)
  }
  
  implicit def wrapStringInLiteralBuilder(lexicalForm: String): LiteralBuilder = new LiteralBuilder(lexicalForm)

  
}