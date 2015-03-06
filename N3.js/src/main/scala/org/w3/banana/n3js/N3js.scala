package org.w3.banana
package n3js

import scala.scalajs.js

/** A N3.js-based implementation of the RDF model.
  * 
  * For now, only the URIs and Literal are natively handled by N3.js.
  */
trait N3js extends RDF {

  // types related to the RDF datamodel
  type Graph = plantain.model.Graph[Node, URI, Node]
  type Triple = (Node, URI, Node)
  type Node = Any
  type URI = String
  type BNode = model.BNode
  type Literal = String
  type Lang = String

  type MGraph = model.MGraph[Node, URI, Node]

  // types for the graph traversal API
  type NodeMatch = Node
  type NodeAny = Null

}

object N3js extends N3jsModule
