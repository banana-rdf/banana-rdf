package org.w3.banana.jena

import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Node => JenaNode, Triple => JenaTriple, _ }
import com.hp.hpl.jena.query.{ QuerySolution, ResultSet, Query => JenaQuery }
import com.hp.hpl.jena.update.UpdateRequest
import org.w3.banana._

trait Jena extends RDF {
  // types related to the RDF datamodel
  type Graph = JenaGraph
  type Triple = JenaTriple
  type Node = JenaNode
  type URI = Node_URI
  type BNode = Node_Blank
  type Literal = Node_Literal
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = JenaNode
  type NodeAny = Node_ANY

  // types related to Sparql
  type Query = JenaQuery
  type SelectQuery = JenaQuery
  type ConstructQuery = JenaQuery
  type AskQuery = JenaQuery
  type UpdateQuery = UpdateRequest
  type Solution = QuerySolution
  type Solutions = ResultSet
}

object Jena extends JenaModule
