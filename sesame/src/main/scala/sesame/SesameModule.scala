package org.w3.banana.sesame

import org.openrdf.repository.RepositoryConnection
import org.w3.banana._

trait SesameModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    // with SparqlHttpModule
    with RDFXMLReaderModule
    with TurtleReaderModule
    // with ReaderSelectorModule
    with RDFXMLWriterModule
    with TurtleWriterModule
    // with WriterSelectorModule
    with JsonSolutionsWriterModule
    with XmlSolutionsWriterModule
    with JsonQueryResultsReaderModule
    with XmlQueryResultsReaderModule {

  type Rdf = Sesame

  implicit val ops: SesameOps = new SesameOps

  implicit val recordBinder: binder.RecordBinder[Sesame] = binder.RecordBinder[Sesame]

  implicit val sparqlOps: SparqlOps[Sesame] = SesameSparqlOps

  implicit val sparqlGraph: SparqlEngine[Sesame, Sesame#Graph] = new SesameGraphSparqlEngine

  implicit val rdfStore: RDFStore[Sesame, RepositoryConnection] = new SesameStore

  implicit val rdfXMLReader: RDFReader[Sesame, RDFXML] = new SesameRDFXMLReader

  implicit val turtleReader: RDFReader[Sesame, Turtle] = new SesameTurtleReader

  implicit val sesameRDFWriterHelper = new SesameRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Sesame, RDFXML] = sesameRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Sesame, Turtle] = sesameRDFWriterHelper.turtleWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerJson] =
    SesameSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Sesame, SparqlAnswerXml] =
    SesameSolutionsWriter.solutionsWriterXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerJson] =
    SesameQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Sesame, SparqlAnswerXml] =
    SesameQueryResultsReader.queryResultsReaderXml

}
