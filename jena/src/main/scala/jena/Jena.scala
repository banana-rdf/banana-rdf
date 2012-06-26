package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _}
import com.hp.hpl.jena.sparql.core.DatasetGraph

trait Jena extends RDF {
  type Graph = JenaGraph
  type Triple = JenaTriple
  type Node = JenaNode
  type URI = Node_URI
  type BNode = Node_Blank
  type Literal = Node_Literal
  type TypedLiteral = Node_Literal
  type LangLiteral = Node_Literal
  type Lang = String
}


object Jena {

  implicit val ops: RDFOperations[Jena] = JenaOperations

  implicit val diesel: Diesel[Jena] = JenaDiesel

  implicit val sparqlOps: SPARQLOperations[Jena, JenaSPARQL] = JenaSPARQLOperations

  implicit val graphQuery: RDFGraphQuery[Jena, JenaSPARQL] = JenaGraphQuery

}
