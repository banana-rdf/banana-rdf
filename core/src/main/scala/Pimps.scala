package org.w3.rdf

import org.w3.algebraic._

class Pimps[M <: Module](val m: M) {
  
  import m._
  
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




object PimpsForSimpleModule extends Pimps[SimpleModule.type](SimpleModule)