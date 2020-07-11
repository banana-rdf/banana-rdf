package org.w3.banana.rdf4j

import org.eclipse.rdf4j.repository.RepositoryConnection
//import org.eclipse.rdf4j.rio.jsonld.JSONLDParser
import org.w3.banana._
import org.w3.banana.io.{SparqlQueryResultsReader, _}
import org.w3.banana.rdf4j.io.{Rdf4jJSONLDReader, Rdf4jQueryResultsReader, Rdf4jRDFWriterHelper, Rdf4jRDFXMLReader, Rdf4jSolutionsWriter, Rdf4jTurtleReader}

import scala.util.Try

trait Rdf4jModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    // with SparqlHttpModule
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

  implicit val ntriplesReader: RDFReader[Rdf4j, Try, NTriples] = new NTriplesReader

  implicit val jsonldReader: RDFReader[Rdf, Try, JsonLd] = new Rdf4jJSONLDReader

  implicit val rdf4jRDFWriterHelper = new Rdf4jRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Rdf4j, Try, RDFXML] = rdf4jRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Rdf4j, Try, Turtle] = rdf4jRDFWriterHelper.turtleWriter

  implicit val ntriplesWriter: RDFWriter[Rdf4j, Try, NTriples] = new NTriplesWriter

  implicit val jsonldCompactedWriter: RDFWriter[Rdf4j, Try, JsonLdCompacted] = rdf4jRDFWriterHelper.jsonldCompactedWriter

  implicit val jsonldExpandedWriter: RDFWriter[Rdf4j, Try, JsonLdExpanded] = rdf4jRDFWriterHelper.jsonldExpandedWriter

  implicit val jsonldFlattenedWriter: RDFWriter[Rdf4j, Try, JsonLdFlattened] = rdf4jRDFWriterHelper.jsonldFlattenedWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerJson] =
    Rdf4jSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Rdf4j, SparqlAnswerXml] =
    Rdf4jSolutionsWriter.solutionsWriterXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Rdf4j, SparqlAnswerJson] =
    Rdf4jQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Rdf4j, SparqlAnswerXml] =
    Rdf4jQueryResultsReader.queryResultsReaderXml

}
