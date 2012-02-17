package org.w3.rdf

import org.w3.algebraic._

class Pimps[M <: Module](val m: M) {
  
  import m._
  
  def prefixBuilder(prefix: String)(value: String): IRI = IRI(prefix+value)
  
  val xsd = prefixBuilder("http://www.w3.org/2001/XMLSchema#") _
  val rdf = prefixBuilder("http://www.w3.org/1999/02/22-rdf-syntax-ns#") _
  
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
  
  implicit def wrapIntAsLiteral(i: Int): TypedLiteral = TypedLiteral(i.toString, xsd("int"))
  
}




object PimpsForSimpleModule extends Pimps[SimpleModule.type](SimpleModule)