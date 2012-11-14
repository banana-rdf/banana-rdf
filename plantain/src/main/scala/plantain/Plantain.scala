package org.w3.banana.plantain

import org.w3.banana._

trait Plantain extends RDF {
  // types related to the RDF datamodel
  type Graph = plantain.Graph
  type Triple = plantain.Triple
  type Node = plantain.Node
  type URI = plantain.URI
  type BNode = plantain.BNode
  type Literal = plantain.Literal
  type TypedLiteral = plantain.TypedLiteral
  type LangLiteral = plantain.LangLiteral
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = plantain.NodeMatch
  type NodeAny = plantain.ANY.type

//  // types related to Sparql
//  type Query = JenaQuery
//  type SelectQuery = JenaQuery
//  type ConstructQuery = JenaQuery
//  type AskQuery = JenaQuery
//  type Solution = QuerySolution
//  type Solutions = ResultSet
}

object Plantain {

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val diesel: Diesel[Plantain] = PlantainDiesel

//  implicit val sparqlOps: SparqlOps[Jena] = JenaSparqlOps
//
//  implicit val graphQuery: RDFGraphQuery[Jena] = JenaGraphSparqlEngine
//
//  implicit val rdfxmlReader: RDFReader[Jena, RDFXML] = JenaRDFReader.rdfxmlReader
//
//  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader.turtleReader
//
//  implicit val readerSelector: ReaderSelector[Jena] = JenaRDFReader.selector //
//
//  implicit val rdfxmlWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter.rdfxmlWriter
//
//  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter.turtleWriter
//
//  implicit val rdfWriterSelector: RDFWriterSelector[Jena] = JenaRDFWriter.selector //
//
//  implicit val solutionsWriterJson: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
//    JenaSolutionsWriter.solutionsWriterJson
//
//  implicit val solutionsWriterXml: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
//    JenaSolutionsWriter.solutionsWriterXml
//
//  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] = JenaSolutionsWriter.solutionsWriterSelector
//
//  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
//    JenaQueryResultsReader.queryResultsReaderJson
//
//  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
//    JenaQueryResultsReader.queryResultsReaderXml



}
