package org.w3.banana.simple

import java.util.concurrent.atomic._

object SimpleModule {
  private val counter = new AtomicLong(1)
  private var prefix = "a"
  private val iterateAt = Long.MaxValue - 1000000
  /**
   * nextId generates unique ids per class loader with minimal use of synchronization.
   * Does one need more uniqueness for bnode creation?
   * @return  the next id
   */
  private def nextId = {
    def next(s: String): String = {
      if (s == "z") "aa";
      else {
        val root = s.substring(0,s.length - 1);
        if (s.last == 'z')
         next(root) + 'a';
        else root + (s.last + 1).asInstanceOf[Char];
      }
    }
    if (counter.get() > iterateAt) synchronized {
       prefix = next(prefix)
       counter.set(0)
    }
    prefix+counter.getAndIncrement
  }

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

  case class BNode(label: String =nextId) extends Node

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