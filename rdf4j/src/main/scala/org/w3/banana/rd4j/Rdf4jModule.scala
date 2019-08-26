package org.w3.banana.rd4j

import org.eclipse.rdf4j.repository.RepositoryConnection
import org.w3.banana.{JsonQueryResultsReaderModule, XmlQueryResultsReaderModule, XmlSolutionsWriterModule, _}
import org.w3.banana.io._
import org.w3.banana.rd4j.io._

import scala.util.Try

trait Rdf4jModule
  extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    with NTriplesReaderModule
    with JsonLDReaderModule
    with RDFXMLWriterModule
    with TurtleWriterModule
    with NTriplesWriterModule
    with JsonLDWriterModule
    with JsonSolutionsWriterModule
    with XmlSolutionsWriterModule
    with JsonQueryResultsReaderModule
    with XmlQueryResultsReaderModule {

  type Rdf = Rdf4j

  implicit val ops: Rdf4jOps = new Rdf4jOps

  implicit val recordBinder: binder.RecordBinder[Rdf4j] = binder.RecordBinder[Rdf4j]

  implicit val sparqlOps: SparqlOps[Rdf4j] = Rdf4jSparqlOps

  implicit val sparqlGraph: SparqlEngine[Rdf4j, Try, Rdf4j#Graph] = Rdf4jGraphSparqlEngine()

  implicit val rdfStore: RDFStore[Rdf4j, Try, RepositoryConnection] with SparqlUpdate[Rdf4j, Try, RepositoryConnection] = new Rdf4jStore

  implicit val rdfXMLReader: RDFReader[Rdf4j, Try, RDFXML] = new Rdf4jRDFXMLReader

  implicit val turtleReader: RDFReader[Rdf4j, Try, Turtle] = new Rdf4jTurtleReader

  implicit val jsonldReader: RDFReader[Rdf4j, Try, JsonLd] = new Rdf4jJSONLDReader

  implicit val ntriplesReader: RDFReader[Rdf4j, Try, NTriples] = new NTriplesReader

  implicit val sesameRDFWriterHelper = new Rdf4jRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Rdf4j, Try, RDFXML] = sesameRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Rdf4j, Try, Turtle] = sesameRDFWriterHelper.turtleWriter

  implicit val ntriplesWriter: RDFWriter[Rdf4j, Try, NTriples] = new NTriplesWriter

  implicit val jsonldCompactedWriter: RDFWriter[Rdf4j, Try, JsonLdCompacted] = sesameRDFWriterHelper.jsonldCompactedWriter

  implicit val jsonldExpandedWriter: RDFWriter[Rdf4j, Try, JsonLdExpanded] = sesameRDFWriterHelper.jsonldExpandedWriter

  implicit val jsonldFlattenedWriter: RDFWriter[Rdf4j, Try, JsonLdFlattened] = sesameRDFWriterHelper.jsonldFlattenedWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerJson] =
    Rdf4jSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerXml] =
    Rdf4jSolutionsWriter.solutionsWriterXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Rdf4j, SparqlAnswerJson] =
    Rdf4jQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Rdf4j, SparqlAnswerXml] =
    Rdf4jQueryResultsReader.queryResultsReaderXml

}
