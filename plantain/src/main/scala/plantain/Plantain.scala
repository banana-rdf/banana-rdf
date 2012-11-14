package org.w3.banana.plantain

import org.w3.banana._
import com.hp.hpl.jena.query.{ Query => JenaQuery, QueryException, QueryFactory }
import com.hp.hpl.jena.query.{ Query => JenaQuery, QuerySolution, ResultSet }

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

  // types related to Sparql
  type Query = JenaQuery
  type SelectQuery = JenaQuery
  type ConstructQuery = JenaQuery
  type AskQuery = JenaQuery
  type Solution = QuerySolution
  type Solutions = ResultSet
}

object Plantain {

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val diesel: Diesel[Plantain] = PlantainDiesel

  implicit val sparqlOps: SparqlOps[Plantain] = PlantainSparqlOps

//  implicit val graphQuery: RDFGraphQuery[Plantain] = PlantainGraphSparqlEngine

  implicit val rdfxmlReader: RDFReader[Plantain, RDFXML] = PlantainRDFXMLReader

  implicit val turtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

//  implicit val readerSelector: ReaderSelector[Plantain] = PlantainRDFReader.selector

  implicit val rdfxmlWriter: RDFWriter[Plantain, RDFXML] = PlantainRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainRDFWriter.turtleWriter

  implicit val rdfWriterSelector: RDFWriterSelector[Plantain] = PlantainRDFWriter.selector //

//  implicit val solutionsWriterJson: SparqlSolutionsWriter[Plantain, SparqlAnswerJson] =
//    PlantainSolutionsWriter.solutionsWriterJson
//
//  implicit val solutionsWriterXml: SparqlSolutionsWriter[Plantain, SparqlAnswerXml] =
//    PlantainSolutionsWriter.solutionsWriterXml
//
//  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Plantain] = PlantainSolutionsWriter.solutionsWriterSelector
//
//  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Plantain, SparqlAnswerJson] =
//    PlantainQueryResultsReader.queryResultsReaderJson
//
//  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Plantain, SparqlAnswerXml] =
//    PlantainQueryResultsReader.queryResultsReaderXml



}
