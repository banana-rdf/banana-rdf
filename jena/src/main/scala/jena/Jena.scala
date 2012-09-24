package org.w3.banana.jena

import org.w3.banana._
import com.hp.hpl.jena.graph.{ Graph => JenaGraph, Triple => JenaTriple, Node => JenaNode, _ }
import com.hp.hpl.jena.query.{ Query => JenaQuery, QuerySolution, ResultSet }
import com.hp.hpl.jena.sparql.core.DatasetGraph

trait Jena extends RDF {
  // types related to the RDF datamodel
  type Graph = ImmutableJenaGraph
  type Triple = JenaTriple
  type Node = JenaNode
  type URI = Node_URI
  type BNode = Node_Blank
  type Literal = Node_Literal
  type TypedLiteral = Node_Literal
  type LangLiteral = Node_Literal
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = JenaNode
  type NodeAny = Node_ANY

  // types related to SPARQL
  type Query = JenaQuery
  type SelectQuery = JenaQuery
  type ConstructQuery = JenaQuery
  type AskQuery = JenaQuery
  type Solution = QuerySolution
  type Solutions = ResultSet
}

object Jena {

  implicit val ops: RDFOps[Jena] = JenaOperations

  implicit val diesel: Diesel[Jena] = JenaDiesel

  implicit val sparqlOps: SPARQLOps[Jena] = JenaSPARQLOps

  implicit val graphQuery: RDFGraphQuery[Jena] = JenaGraphSPARQLEngine

  implicit val rdfxmlReader: RDFReader[Jena, RDFXML] = JenaRDFReader[RDFXML]

  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader[Turtle]

  implicit val readerSelector: ReaderSelector[Jena] =
    ReaderSelector[Jena, RDFXML] combineWith ReaderSelector[Jena, Turtle]

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter.turtleWriter

  implicit val writerSelector: RDFWriterSelector[Jena] = JenaRDFWriter.writerSelector

}
