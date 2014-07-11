package org.w3.banana.plantain

import org.w3.banana._
import akka.http.model.Uri

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

}

object Plantain extends PlantainModule
