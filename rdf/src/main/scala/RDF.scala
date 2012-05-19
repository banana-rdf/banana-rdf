package org.w3.banana

trait RDF {
  type Graph
  type Triple
  type Node
  type URI <: Node
  type BNode <: Node
  type Literal <: Node
  type TypedLiteral <: Literal
  type LangLiteral <: Literal
  type Lang

  type Store
}
