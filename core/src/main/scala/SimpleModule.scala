package org.w3.rdf

import org.w3.algebraic._
import org.w3.rdf.ScalaModule.Lang

object ScalaModule extends Module {


  case class Graph(triples: Set[Triple]) extends GraphInterface {
    def iterator = triples.iterator
    def ++(other: Graph): Graph = Graph(triples ++ other.triples)
  }

  object Graph extends GraphCompanionObject {
    def empty: Graph = Graph(Set[Triple]())
    def apply(elems: Triple*): Graph = Graph(Set[Triple](elems:_*))
    def apply(it: Iterable[Triple]): Graph = Graph(it.toSet)
  }

  case class Triple (s: Node, p: IRI, o: Node)
  object Triple extends AlgebraicDataType3[Node, IRI, Node, Triple]

  sealed trait Node

  case class IRI(iri: String) extends Node { override def toString = '<' + iri + '>' }
  object IRI extends AlgebraicDataType1[String, IRI]

  case class BNode(label: String) extends Node
  object BNode extends AlgebraicDataType1[String, BNode]

  case class Literal(lexicalForm: String, datatype: LiteralType=xsdStringType) extends Node
  object Literal extends AlgebraicDataType2[String, LiteralType, Literal]
  
  class Lang(val tag: String)
  object Lang extends AlgebraicDataType1[String, Lang] {
    def unapply(lt: Lang): Option[String] = if (lt.tag != null) return Some(lt.tag) else None
    def apply(tag: String): Lang = new Lang(tag)
  }

}
