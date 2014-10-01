package org.w3.banana.plantain.generic

import org.w3.banana._
import org.w3.banana.plantain.model

trait Plantain[UnderlyingUri] extends RDF {

  // types related to the RDF datamodel
  type Graph = model.Graph[UnderlyingUri]
  type Triple = model.Triple[UnderlyingUri]
  type Node = model.Node
  type URI = model.URI[UnderlyingUri]
  type BNode = model.BNode
  type Literal = model.Literal[UnderlyingUri]
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = model.NodeMatch
  type NodeAny = model.ANY.type

}

