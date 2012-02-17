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
  
}




object PimpsForSimpleModule extends Pimps[SimpleModule.type](SimpleModule)