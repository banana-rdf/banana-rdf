package org.w3.banana.plantain

import akka.http.scaladsl.model.Uri
import org.w3.banana._

/** A Scala-based RDF implementation that limits boxing.
  */
trait Plantain extends RDF {

  // types related to the RDF datamodel
  type Graph = model.IntHexastoreGraph[Node, URI, Node]
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

