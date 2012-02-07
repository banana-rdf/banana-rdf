package org.w3.rdf

import org.w3.algebraic._

object ScalaModel extends Model {


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

  case class NodeIRI(i: IRI) extends Node
  object NodeIRI extends AlgebraicDataType1[IRI, NodeIRI]

  case class NodeBNode(b: BNode) extends Node
  object NodeBNode extends AlgebraicDataType1[BNode, NodeBNode]

  case class NodeLiteral(lit: Literal) extends Node
  object NodeLiteral extends AlgebraicDataType1[Literal, NodeLiteral]

  case class IRI(iri: String) { override def toString = '"' + iri + '"' }
  object IRI extends AlgebraicDataType1[String, IRI]

  case class BNode(label: String)
  object BNode extends AlgebraicDataType1[String, BNode]

  case class Literal(lexicalForm: String, langtag: Option[LangTag], datatype: Option[IRI])
  object Literal extends AlgebraicDataType3[String, Option[LangTag], Option[IRI], Literal]
  
  case class LangTag(s: String)
  object LangTag extends AlgebraicDataType1[String, LangTag]

}
