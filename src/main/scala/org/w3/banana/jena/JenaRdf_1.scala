//package org.w3.banana.jena
//
//import org.apache.jena.graph.{ Graph => JenaGraph, Node => JenaNode, Triple => JenaTriple, _ }
//import org.apache.jena.query.{ QuerySolution, ResultSet, Query => JenaQuery }
//import org.apache.jena.update.UpdateRequest
//
//import org.w3.banana._
//
//sealed trait Rdf4j extends RDF[Rdf4j]
//
//given as GraphTyp[Rdf4j] {
//  type Out = JenaGraph
//}
//
//given as TripleTyp[Rdf4j] { type Out = JenaTriple }
//
//given as NodeTyp[Rdf4j] { type Out = JenaNode }
//
//given as URITyp[Rdf4j] { type Out = Node_URI }
//given as BNodeTyp[Rdf4j] { type Out = Node_Blank }
//given as LiteralTyp[Rdf4j] { type Out = Node_Literal }
//
//given as LangTyp[Rdf4j] { type Out = String }
//
//// mutable graphs
//given as MGraphTyp[Rdf4j] { type Out = JenaGraph }
//
//// types for the graph traversal API
//given as NodeMatchTyp[Rdf4j] { type Out = JenaNode }
//given as NodeAnyTyp[Rdf4j] { type Out = Node_ANY }
//
//// types related to Sparql
//given as QueryTyp[Rdf4j] { type Out = JenaQuery }
//given as SelectQueryTyp[Rdf4j] { type Out = JenaQuery }
//given as ConstructQueryTyp[Rdf4j] { type Out = JenaQuery }
//given as AskQueryTyp[Rdf4j] { type Out = JenaQuery }
//given as UpdateQueryTyp[Rdf4j] { type Out = UpdateRequest }
//given as SolutionTyp[Rdf4j] { type Out = QuerySolution }
//given as SolutionsTyp[Rdf4j] { type Out = ResultSet }