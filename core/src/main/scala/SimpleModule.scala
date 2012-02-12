package org.w3.rdf

import org.w3.algebraic._
import util.{MurmurHash3, MurmurHash}

object SimpleModule extends Module {

  case class Graph(triples: Set[Triple]) extends GraphInterface {
    def iterator = triples.iterator
    def ++(other: Graph): Graph = Graph(triples ++ other.triples)
  }

  object Graph extends GraphCompanionObject {
    def empty: Graph = Graph(Set[Triple]())
    def apply(elems: Triple*): Graph = Graph(Set[Triple](elems:_*))
    def apply(it: Iterable[Triple]): Graph = Graph(it.toSet)
  }
  
  case class Triple(s: Node, p: IRI, o: Node)
  object Triple extends AlgebraicDataType3[Node, IRI, Node, Triple]
  
  sealed trait Node
  
  object Node extends NodeCompanionObject {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T = node match {
      case iri: IRI => funIRI(iri)
      case bnode: BNode => funBNode(bnode)
      case literal: Literal => funLiteral(literal)
    }
  }

  case class IRI(iri: String) extends Node
  object IRI extends AlgebraicDataType1[String, IRI]

  case class BNode(label: String) extends Node
  object BNode extends AlgebraicDataType1[String, BNode]

  sealed trait Literal extends Node {
    val lexicalForm: String
    val datatype: IRI
  }
  
  object Literal extends LiteralCompanionObject {
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T = literal match {
      case tl: TypedLiteral => funTL(tl)
      case ll: LangLiteral => funLL(ll)
    }
  }
  
  case class TypedLiteral(lexicalForm: String, datatype: IRI) extends Literal
  object TypedLiteral extends AlgebraicDataType2[String, IRI, TypedLiteral]
  
  case class LangLiteral(lexicalForm: String, lang: Lang) extends Literal {
    val datatype = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
  }
  object LangLiteral extends AlgebraicDataType2[String, Lang, LangLiteral]

  type Lang = String
  object Lang extends AlgebraicDataType1[String, Lang] {
    def apply(langString: String) = langString
    def unapply(lang: Lang) = Some(lang)
  }

}
