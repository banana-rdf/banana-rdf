package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.{ Graph => SesameGraph, Literal => SesameLiteral, BNode => SesameBNode, _ }
import org.openrdf.repository._

trait Sesame extends RDF {
  type Graph = SesameGraph
  type Triple = Statement
  type Node = Value
  type IRI = URI
  type BNode = SesameBNode
  type Literal = SesameLiteral
  type TypedLiteral = SesameLiteral
  type LangLiteral = SesameLiteral
  type Lang = String

  type Store = Repository
}
