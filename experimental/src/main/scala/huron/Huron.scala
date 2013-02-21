package org.w3.banana.huron

import org.w3.banana._
import play.api.libs.json._

trait Huron extends RDF {
  // types related to the RDF datamodel
  type Graph = Set[Triple]
  type Triple = JsArray
  type Node = JsArray
  type URI = JsArray
  type BNode = JsArray
  type Literal = JsArray
  type TypedLiteral = JsArray
  type LangLiteral = JsArray
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = model.NodeMatch
  type NodeAny = model.ANY.type

//  // types related to Sparql
//  type Query = ParsedQuery
//  type SelectQuery = ParsedTupleQuery
//  type ConstructQuery = ParsedGraphQuery
//  type AskQuery = ParsedBooleanQuery
//  type Solution = BindingSet
//  // instead of TupleQueryResult so that it's eager instead of lazy
//  type Solutions = Iterator[BindingSet]

}

object Huron {

  implicit val ops: RDFOps[Huron] = HuronOps

}
