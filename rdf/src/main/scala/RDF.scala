package org.w3.banana

trait RDF {
  // types related to the RDF datamodel
  type Graph
  type Triple
  type Node
  type URI <: Node
  type BNode <: Node
  type Literal <: Node
  type Lang

  // types for the graph traversal API
  type NodeMatch
  type NodeAny <: NodeMatch

  // types related to Sparql
  type Query
  type SelectQuery <: Query
  type ConstructQuery <: Query
  type AskQuery <: Query
  type UpdateQuery
  type Solution
  type Solutions
}
