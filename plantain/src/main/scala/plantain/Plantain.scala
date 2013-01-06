package org.w3.banana.plantain

import org.w3.banana._
import org.openrdf.query.BindingSet
import org.openrdf.query.parser.{ ParsedQuery, ParsedTupleQuery, ParsedGraphQuery, ParsedBooleanQuery }

trait Plantain extends RDF {
  // types related to the RDF datamodel
  type Graph = model.Graph
  type Triple = model.Triple
  type Node = model.Node
  type URI = model.URI
  type BNode = model.BNode
  type Literal = model.Literal
  type TypedLiteral = model.TypedLiteral
  type LangLiteral = model.LangLiteral
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = model.NodeMatch
  type NodeAny = model.ANY.type

  // types related to Sparql
  type Query = ParsedQuery
  type SelectQuery = ParsedTupleQuery
  type ConstructQuery = ParsedGraphQuery
  type AskQuery = ParsedBooleanQuery
  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Iterator[BindingSet]

}

object Plantain {

  implicit val ops: RDFOps[Plantain] = PlantainOps

  implicit val diesel: Diesel[Plantain] = PlantainDiesel

  implicit val sparqlOps: SparqlOps[Plantain] = PlantainSparqlOps

  implicit val graphQuery: RDFGraphQuery[Plantain] = PlantainGraphSparqlEngine

  implicit val rdfxmlReader: RDFReader[Plantain, RDFXML] = PlantainRDFXMLReader

  implicit val turtleReader: RDFReader[Plantain, Turtle] = PlantainTurtleReader

//  implicit val readerSelector: ReaderSelector[Plantain] = PlantainRDFReader.selector

  implicit val rdfxmlWriter: RDFWriter[Plantain, RDFXML] = PlantainRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Plantain, Turtle] = PlantainRDFWriter.turtleWriter

  implicit val rdfWriterSelector: RDFWriterSelector[Plantain] = PlantainRDFWriter.selector //

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Plantain, SparqlAnswerJson] =
    PlantainSolutionsWriter.solutionsWriterJson

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Plantain, SparqlAnswerXml] =
    PlantainSolutionsWriter.solutionsWriterXml

  implicit val solutionsWriterSelector: SparqlSolutionsWriterSelector[Plantain] = PlantainSolutionsWriter.writerSelector

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Plantain, SparqlAnswerJson] =
    PlantainQueryResultsReader.queryResultsReaderJson

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Plantain, SparqlAnswerXml] =
    PlantainQueryResultsReader.queryResultsReaderXml

}
