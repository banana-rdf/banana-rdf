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

  // types related to Sparql
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

  implicit val sparqlOps: SparqlOps[Jena] = JenaSparqlOps

  implicit val graphQuery: RDFGraphQuery[Jena] = JenaGraphSparqlEngine

  implicit val rdfxmlReader: RDFReader[Jena, RDFXML] = JenaRDFReader.rdfxmlReader

  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader.turtleReader

  implicit val readerSelector: ReaderSelector[Jena] = JenaRDFReader.selector //

  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter.turtleWriter

  implicit val rdfWriterSelector: RDFWriterSelector[Jena] = JenaRDFWriter.selector //

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter.solutionsWriterJson

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter.solutionsWriterXml

  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] = JenaSolutionsWriter.solutionsWriterSelector

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    JenaQueryResultsReader.queryResultsReaderJson

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    JenaQueryResultsReader.queryResultsReaderXml



}
