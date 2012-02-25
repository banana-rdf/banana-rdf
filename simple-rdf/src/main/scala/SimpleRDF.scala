package org.w3.rdf.simple

import org.w3.rdf._

trait SimpleRDF extends RDF {
  type Graph = SimpleModule.Graph
  type Triple = SimpleModule.Triple
  type Node = SimpleModule.Node
  type IRI = SimpleModule.IRI
  type BNode = SimpleModule.BNode
  type Literal = SimpleModule.Literal
  type TypedLiteral = SimpleModule.TypedLiteral
  type LangLiteral = SimpleModule.LangLiteral
  type Lang = SimpleModule.Lang
  
}