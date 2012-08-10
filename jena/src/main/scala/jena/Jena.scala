package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _ }
import com.hp.hpl.jena.query.{ Query => JenaQuery, QuerySolution, ResultSet }
import com.hp.hpl.jena.sparql.core.DatasetGraph

trait Jena extends RDF {
  type Graph = ImmutableJenaGraph
  type Triple = JenaTriple
  type Node = JenaNode
  type URI = Node_URI
  type BNode = Node_Blank
  type Literal = Node_Literal
  type TypedLiteral = Node_Literal
  type LangLiteral = Node_Literal
  type Lang = String

  type Query = JenaQuery
  type SelectQuery = JenaQuery
  type ConstructQuery = JenaQuery
  type AskQuery = JenaQuery
  type Solution = QuerySolution
  type Solutions = ResultSet
}

object Jena {

  implicit val ops: RDFOperations[Jena] = JenaOperations

  implicit val diesel: Diesel[Jena] = Diesel[Jena]

  implicit val sparqlOps: SPARQLOperations[Jena] = JenaSPARQLOperations

  implicit val graphQuery: RDFGraphQuery[Jena] = JenaGraphQuery

}
