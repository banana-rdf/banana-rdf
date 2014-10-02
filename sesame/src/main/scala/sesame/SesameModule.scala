package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana._

import scala.concurrent.ExecutionContext.Implicits.global

trait SesameModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    // with SparqlHttpModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    with ReaderSelectorModule
    with RDFXMLWriterModule
    with TurtleWriterModule
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

  implicit val rdfXMLReader: RDFReader[Sesame, RDFXML] = SesameRDFXMLReader

  implicit val turtleReader: RDFReader[Sesame, Turtle] = SesameTurtleReader

  implicit val jsonldCompactReader: RDFReader[Sesame, JSONLD_COMPACTED] = SesameJSONLDCompactedReader

  implicit val jsonldExpandedReader: RDFReader[Sesame, JSONLD_EXPANDED] = SesameJSONLDExpandedReader

  implicit val jsonldFlattenedReader: RDFReader[Sesame, JSONLD_FLATTENED] = SesameJSONLDFlattenedReader

  implicit val sesameRDFWriterHelper = new SesameRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Sesame, RDFXML] = sesameRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Sesame, Turtle] = sesameRDFWriterHelper.turtleWriter

  implicit val jsonldCompactedWriter: RDFWriter[Sesame, JSONLD_COMPACTED] = sesameRDFWriterHelper.jsonldCompactedWriter

  implicit val jsonldExpandedWriter: RDFWriter[Sesame, JSONLD_EXPANDED] = sesameRDFWriterHelper.jsonldExpandedWriter

  implicit val jsonldFlattenedWriter: RDFWriter[Sesame, JSONLD_FLATTENED] = sesameRDFWriterHelper.jsonldFlattenedWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val readerSelector: ReaderSelector[Rdf] = ReaderSelector[Sesame, Turtle] combineWith
    ReaderSelector[Sesame, RDFXML] combineWith ReaderSelector[Sesame, JSONLD_COMPACTED] combineWith
    ReaderSelector[Sesame, JSONLD_EXPANDED] combineWith ReaderSelector[Sesame, JSONLD_FLATTENED]

  implicit val writerSelector: RDFWriterSelector[Rdf] = sesameRDFWriterHelper.selector

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
