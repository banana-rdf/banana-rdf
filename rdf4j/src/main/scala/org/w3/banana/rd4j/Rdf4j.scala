package org.w3.banana.rd4j

import org.eclipse.rdf4j.model.{Model, Statement, Value}
import org.eclipse.rdf4j.model.{BNode => Rdf4jBNode, IRI => Rdf4jIRI, Literal => Rdf4jLiteral, _}
import org.eclipse.rdf4j.query.BindingSet
import org.eclipse.rdf4j.query.parser.{ParsedBooleanQuery, ParsedGraphQuery, ParsedQuery, ParsedTupleQuery, ParsedUpdate}
import org.w3.banana.RDF

trait Rdf4j extends RDF {


  type Graph = Model
  type Triple = Statement
  type Node = Value
  type URI = Rdf4jIRI
  type BNode = Rdf4jBNode
  type Literal = Rdf4jLiteral
  type Lang = String


  // mutable graphs
  type MGraph = Model

  // types for the graph traversal API
  type NodeMatch = Value
  type NodeAny = Null
  type NodeConcrete = Value

  // types related to Sparql
  type Query = ParsedQuery
  type SelectQuery = ParsedTupleQuery
  type ConstructQuery = ParsedGraphQuery
  type AskQuery = ParsedBooleanQuery
  type UpdateQuery = ParsedUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Stream[BindingSet]

}

object Rdf4j extends Rdf4jModule