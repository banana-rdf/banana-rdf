package org.w3.rdf.simple

import org.w3.rdf._

object SimpleModule {

  type Graph = Set[Triple]
  
  object Graph {
    def empty: Graph = Set[Triple]()
    def apply(it: Iterable[Triple]): Graph = it.toSet
    def toIterable(graph: Graph): Iterable[Triple] = graph.toIterable
  }
  
  case class Triple(s: Node, p: IRI, o: Node)
  
  sealed trait Node
  
  object Node {
    def fold[T](node: Node)(funIRI: IRI => T, funBNode: BNode => T, funLiteral: Literal => T): T = node match {
      case iri: IRI => funIRI(iri)
      case bnode: BNode => funBNode(bnode)
      case literal: Literal => funLiteral(literal)
    }
  }

  case class IRI(iri: String) extends Node

  case class BNode(label: String) extends Node

  sealed trait Literal extends Node {
    val lexicalForm: String
    val datatype: IRI
  }
  
  object Literal {
    def fold[T](literal: Literal)(funTL: TypedLiteral => T, funLL: LangLiteral => T): T = literal match {
      case tl: TypedLiteral => funTL(tl)
      case ll: LangLiteral => funLL(ll)
    }
  }
  
  case class TypedLiteral(lexicalForm: String, datatype: IRI) extends Literal
  
  case class LangLiteral(lexicalForm: String, lang: Lang) extends Literal {
    val datatype = IRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
  }

  type Lang = String
  object Lang {
    def apply(langString: String) = langString
    def unapply(lang: Lang) = Some(lang)
  }
 
}