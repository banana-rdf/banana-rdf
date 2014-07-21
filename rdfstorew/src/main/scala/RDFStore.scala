package org.w3.banana.rdfstorew

import org.w3.banana._
import scala.scalajs.js

sealed trait JsNodeMatch

case class PlainNode(node: RDFStore#Node) extends JsNodeMatch

case object JsANY extends JsNodeMatch


trait RDFStore extends RDF {
  // types related to the RDF datamodel
  type Graph = RDFStoreGraph
  type Triple = RDFStoreTriple
  type Node = RDFStoreRDFNode
  type URI = RDFStoreNamedNode
  type BNode = RDFStoreBlankNode
  type Literal = RDFStoreLiteral
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = JsNodeMatch
  type NodeAny = JsANY.type
  type NodeConcrete = JsNodeMatch

  // types related to Sparql
  type Query = String
  type SelectQuery = String
  type ConstructQuery = String
  type AskQuery = String
  type UpdateQuery = String

  type Solution = Nothing

  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Nothing
}

object RDFStore extends RDFStoreModule
