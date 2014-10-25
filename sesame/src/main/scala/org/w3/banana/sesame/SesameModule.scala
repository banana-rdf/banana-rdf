package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana._
import org.w3.banana.io.SparqlQueryResultsReader
import org.w3.banana.sesame.io._
import org.w3.banana.io._
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

trait SesameModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    // with SparqlHttpModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    with JsonLDReaderModule
    with ReaderSelectorModule
    with RDFXMLWriterModule
    with TurtleWriterModule
    with JsonLDWriterModule
    with WriterSelectorModule
    with JsonSolutionsWriterModule
    with XmlSolutionsWriterModule
    with JsonQueryResultsReaderModule
    with XmlQueryResultsReaderModule {

  type Rdf = Sesame

  implicit val ops: SesameOps = new SesameOps

  implicit val recordBinder: binder.RecordBinder[Sesame] = binder.RecordBinder[Sesame]

  implicit val sparqlOps: SparqlOps[Sesame] = SesameSparqlOps

  implicit val sparqlGraph: SparqlEngine[Sesame, Sesame#Graph] = SesameGraphSparqlEngine()

  implicit val rdfStore: RDFStore[Sesame, RepositoryConnection] with SparqlUpdate[Sesame, RepositoryConnection] = new SesameStore

  implicit val rdfXMLReader: RDFReader[Sesame, Try, RDFXML] = new SesameRDFXMLReader

  implicit val turtleReader: RDFReader[Sesame, Try, Turtle] = new SesameTurtleReader

  implicit val jsonldCompactReader: RDFReader[Sesame, Try, JsonLdCompacted] = new SesameJSONLDCompactedReader

  implicit val jsonldExpandedReader: RDFReader[Sesame, Try, JsonLdExpanded] = new SesameJSONLDExpandedReader

  implicit val jsonldFlattenedReader: RDFReader[Sesame, Try, JsonLdFlattened] = new SesameJSONLDFlattenedReader

  implicit val sesameRDFWriterHelper = new SesameRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Sesame, RDFXML] = sesameRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Sesame, Turtle] = sesameRDFWriterHelper.turtleWriter

  implicit val jsonldCompactedWriter: RDFWriter[Sesame, JsonLdCompacted] = sesameRDFWriterHelper.jsonldCompactedWriter

  implicit val jsonldExpandedWriter: RDFWriter[Sesame, JsonLdExpanded] = sesameRDFWriterHelper.jsonldExpandedWriter

  implicit val jsonldFlattenedWriter: RDFWriter[Sesame, JsonLdFlattened] = sesameRDFWriterHelper.jsonldFlattenedWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val readerSelector: ReaderSelector[Sesame, Try] =
    ReaderSelector[Sesame, Try, Turtle] combineWith
    ReaderSelector[Sesame, Try, RDFXML] combineWith
    ReaderSelector[Sesame, Try, JsonLdCompacted] combineWith
    ReaderSelector[Sesame, Try, JsonLdExpanded] combineWith
    ReaderSelector[Sesame, Try, JsonLdFlattened]

  implicit val writerSelector: RDFWriterSelector[Sesame] = sesameRDFWriterHelper.selector

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
