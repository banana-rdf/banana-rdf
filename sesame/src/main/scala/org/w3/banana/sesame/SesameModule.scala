package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana._
import org.w3.banana.io.{SparqlQueryResultsReader, _}
import org.w3.banana.sesame.io._

import scala.util.Try

trait SesameModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    // with SparqlHttpModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    with NTriplesReaderModule
    with JsonLDReaderModule
    with ReaderSelectorModule
    with SparqlSolutionsWriterSelectorModule
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

  implicit val sparqlGraph: SparqlEngine[Sesame, Try, Sesame#Graph]
    with SparqlUpdate[Rdf,Try, Rdf#Graph] = SesameGraphSparqlEngine()

  implicit val rdfStore: RDFStore[Sesame, Try, RepositoryConnection] with SparqlUpdate[Sesame, Try, RepositoryConnection] = new SesameStore

  implicit val rdfXMLReader: RDFReader[Sesame, Try, RDFXML] = new SesameRDFXMLReader

  implicit val turtleReader: RDFReader[Sesame, Try, Turtle] = new SesameTurtleReader

  implicit val jsonldReader: RDFReader[Sesame, Try, JsonLd] = new SesameJSONLDReader

  implicit val ntriplesReader: RDFReader[Sesame, Try, NTriples] = new NTriplesReader

  implicit val sesameRDFWriterHelper = new SesameRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Sesame, Try, RDFXML] = sesameRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Sesame, Try, Turtle] = sesameRDFWriterHelper.turtleWriter

  implicit val jsonldCompactedWriter: RDFWriter[Sesame, Try, JsonLdCompacted] = sesameRDFWriterHelper.jsonldCompactedWriter

  implicit val jsonldExpandedWriter: RDFWriter[Sesame, Try, JsonLdExpanded] = sesameRDFWriterHelper.jsonldExpandedWriter

  implicit val jsonldFlattenedWriter: RDFWriter[Sesame, Try, JsonLdFlattened] = sesameRDFWriterHelper.jsonldFlattenedWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val sparqlSolutionsWriterSelector: SparqlSolutionsWriterSelector[Sesame] =
      SparqlSolutionWriterSelector[Sesame,SparqlAnswerJson] combineWith
        SparqlSolutionWriterSelector[Sesame,SparqlAnswerXml]


  implicit val readerSelector: ReaderSelector[Sesame, Try] =
    ReaderSelector[Sesame, Try, Turtle] combineWith
    ReaderSelector[Sesame, Try, RDFXML] combineWith
    ReaderSelector[Sesame, Try, JsonLd]

  implicit val writerSelector: org.w3.banana.RDFWriterSelector[Sesame, Try] = sesameRDFWriterHelper.selector

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
