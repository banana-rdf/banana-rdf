//package org.w3.banana.jena
//
//import org.apache.jena.graph.{Graph as JenaGraph, Node as JenaNode, Triple as JenaTriple, *}
//import org.apache.jena.query.{QuerySolution, ResultSet, Query as JenaQuery}
//import org.apache.jena.update.UpdateRequest
//import org.w3.banana.{RDF, *}
//
//object Jena extends RDF {
//  // types related to the RDF datamodel
//  type Graph = JenaGraph
//  type Triple = JenaTriple
//  type Node = JenaNode
//  type URI = Node_URI
//  type BNode = Node_Blank
//  type Literal = Node_Literal
//  type Lang = String
//
//  // mutable graphs
//  type MGraph = JenaGraph
//
//  // types for the graph traversal API
//  type NodeMatch = JenaNode
//  type NodeAny = Node_ANY
//
//  // types related to Sparql
//  type Query = JenaQuery
//  type SelectQuery = JenaQuery
//  type ConstructQuery = JenaQuery
//  type AskQuery = JenaQuery
//  type UpdateQuery = UpdateRequest
//  type Solution = QuerySolution
//  type Solutions = ResultSet
//}
//
