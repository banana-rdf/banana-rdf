package org.w3.banana.plantain

import org.w3.banana._
import java.net.URI

/** A Scala-based RDF implementation that limits boxing.
  */
trait Plantain extends RDF {

  // types related to the RDF datamodel
  type Graph = model.IntHexastoreGraph[Node, URI, Node]
  type Triple = (Node, URI, Node)
  type Node = Any
  type URI = java.net.URI
  type BNode = model.BNode
  // TODO it's a lot of work, but we should be able to use Any here
  type Literal = model.Literal
  type Lang = String

  type MGraph = model.MGraph[Node, URI, Node]

  // types for the graph traversal API
  type NodeMatch = Node
  type NodeAny = Null

}

object Plantain extends PlantainModule
