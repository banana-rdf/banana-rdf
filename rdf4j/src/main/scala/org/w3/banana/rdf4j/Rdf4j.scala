package org.w3.banana.rdf4j

import org.eclipse.rdf4j.model.{ BNode => Rdf4jBNode, Literal => Rdf4jLiteral, URI => Rdf4jURI, IRI => Rdf4jIRI, _ }
import org.eclipse.rdf4j.query._
import org.eclipse.rdf4j.query.parser._
import org.w3.banana._
import org.w3.banana.rdf4j._

case class Rdf4jParseUpdate(query: String)

trait Rdf4j extends RDF {
  // types related to the RDF datamodel
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

  //FIXME Can't use ParsedUpdate because of https://openrdf.atlassian.net/browse/SES-1847
  type UpdateQuery = Rdf4jParseUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Vector[BindingSet]
}

object Rdf4j extends Rdf4jModule
