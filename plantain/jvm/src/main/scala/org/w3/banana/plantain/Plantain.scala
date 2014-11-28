package org.w3.banana.plantain

import org.w3.banana._
import akka.http.model.Uri

/** A Scala-based RDF implementation that limits boxing.
  */
trait Plantain extends RDF {

  // types related to the RDF datamodel
  type Graph = model.Graph[Node, URI, Node]
  type Triple = (Node, URI, Node)
  type Node = Any
  type URI = Uri
  type BNode = model.BNode
  type Literal = Any
  type Lang = String

  type MGraph = model.MGraph[Node, URI, Node]

  // types for the graph traversal API
  type NodeMatch = Node
  type NodeAny = Null

}

object Plantain extends PlantainModule

