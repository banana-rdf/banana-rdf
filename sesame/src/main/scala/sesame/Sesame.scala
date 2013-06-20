package org.w3.banana.sesame

import org.w3.banana._
import org.openrdf.model.{ Literal => SesameLiteral, BNode => SesameBNode, URI => SesameURI, _ }
import org.openrdf.repository._
import org.openrdf.query._
import org.openrdf.query.parser._
import info.aduna.iteration.CloseableIteration

trait Sesame extends RDF {
  // types related to the RDF datamodel
  type Graph = Model
  type Triple = Statement
  type Node = Value
  type URI = SesameURI
  type BNode = SesameBNode
  type Literal = SesameLiteral
  type TypedLiteral = SesameLiteral
  type LangLiteral = SesameLiteral
  type Lang = String

  // types for the graph traversal API
  type NodeMatch = Value
  type NodeAny = Null
  type NodeConcrete = Value

  // types related to Sparql
  type Query = ParsedQuery
  type SelectQuery = ParsedTupleQuery
  type ConstructQuery = ParsedGraphQuery
  type AskQuery = ParsedBooleanQuery
  type Solution = BindingSet
  // instead of TupleQueryResult so that it's eager instead of lazy
  type Solutions = Vector[BindingSet]
}

object Sesame {

  implicit val ops: RDFOps[Sesame] = SesameOperations

  implicit val recordBinder: binder.RecordBinder[Sesame] = binder.RecordBinder[Sesame]

  implicit val sparqlOps: SparqlOps[Sesame] = SesameSparqlOps

  implicit val sparqlGraph: SparqlGraph[Sesame] = SesameSparqlGraph

  implicit val rdfxmlReader: RDFReader[Sesame, RDFXML] = SesameRDFXMLReader

  implicit val turtleReader: RDFReader[Sesame, Turtle] = SesameTurtleReader

  implicit val jsonldReader: RDFReader[Sesame, JSONLD] = SesameJSONLDReader

  implicit val rdfxmlWriter: RDFWriter[Sesame, RDFXML] = SesameRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Sesame, Turtle] = SesameRDFWriter.turtleWriter

  implicit val jsonldWriter: RDFWriter[Sesame, JSONLD] = SesameRDFWriter.jsonldWriter

  implicit val solutionsWriterJson: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val solutionsWriterXml: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val queryResultsReaderJson: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val queryResultsReaderXml: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
