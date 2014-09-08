package org.w3.banana.sesame

import org.openrdf.model.{ BNode => SesameBNode, Literal => SesameLiteral, URI => SesameURI, _ }
import org.openrdf.query._
import org.openrdf.query.parser._
import org.w3.banana._

case class SesameParseUpdate(query: String)

trait Sesame extends RDF {
  // types related to the RDF datamodel
  type Graph = Model
  type Triple = Statement
  type Node = Value
  type URI = SesameURI
  type BNode = SesameBNode
  type Literal = SesameLiteral
  type Lang = String

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
  type UpdateQuery = SesameParseUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Vector[BindingSet]
}

object Sesame extends SesameModule
