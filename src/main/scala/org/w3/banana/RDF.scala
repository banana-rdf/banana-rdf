package org.w3.banana

import org.apache.jena.graph.{Node_Blank, Node_Literal, Node_URI}

trait RDF {
  // types related to the RDF datamodel
  type Graph
  type Triple
  type Node 
  type URI = Node_URI
  type BNode = Node_Blank
  type Literal = Node_Literal
  type Lang

  // mutable graphs
  type MGraph <: AnyRef

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
