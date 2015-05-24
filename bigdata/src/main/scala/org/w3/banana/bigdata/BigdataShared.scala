package org.w3.banana.bigdata

import com.bigdata.rdf.model._
import org.w3.banana.{RDFOpsModule, RDFModule, RDF}

/**
 * Shared classes between Bigdata Remote and Local
 */
trait BigdataShared
{
  self:RDF=>

  // types related to the RDF datamodel
  type Graph = BigdataGraph
  type Triple = BigdataStatement
  type Node = BigdataValue
  type URI = BigdataURI
  type BNode = BigdataBNode
  type Literal = BigdataLiteral
  type Lang = String

  type MGraph = BigdataGraph

  // types for the graph traversal API
  type NodeMatch = BigdataValue
  type NodeAny = Null
  type NodeConcrete = BigdataValue


}

 //could not inherit from prefix because it uses ops