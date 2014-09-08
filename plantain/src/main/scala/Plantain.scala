package org.w3.banana.plantain

import org.openrdf.query.BindingSet
import org.openrdf.query.parser._
import org.w3.banana._

case class SesameParseUpdate(query: String)

trait Plantain extends RDF {

  // types related to the RDF datamodel
  type Graph = model.Graph
  type Triple = model.Triple
  type Node = model.Node
  type URI = model.URI
  type BNode = model.BNode
  type Literal = model.Literal
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = model.NodeMatch
  type NodeAny = model.ANY.type

  // types related to Sparql
  type Query = ParsedQuery
  type SelectQuery = ParsedTupleQuery
  type ConstructQuery = ParsedGraphQuery
  type AskQuery = ParsedBooleanQuery

  // FIXME added just to avoid compilation error
  type UpdateQuery = SesameParseUpdate

  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = BoundSolutions

}

case class BoundSolutions(iterator: Iterator[BindingSet], bindings: List[String])

object Plantain extends PlantainModule
