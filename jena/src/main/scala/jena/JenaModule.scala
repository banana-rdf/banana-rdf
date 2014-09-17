package org.w3.banana.jena

import com.hp.hpl.jena.query.Dataset
import org.w3.banana._

import scala.concurrent.ExecutionContext.Implicits.global

trait JenaModule
    extends RDFModule
    with RDFOpsModule
    with RecordBinderModule
    with SparqlGraphModule
    with SparqlHttpModule
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

  type Rdf = Jena

  implicit val ops: JenaOps = new JenaOps

  implicit val jenaUtil: JenaUtil = new JenaUtil

  implicit val recordBinder: binder.RecordBinder[Jena] = binder.RecordBinder[Jena]

  implicit val sparqlOps: SparqlOps[Jena] = new JenaSparqlOps

  implicit val sparqlGraph: SparqlEngine[Jena, Jena#Graph] = JenaGraphSparqlEngine(ops)

  import java.net.URL
  implicit val sparqlHttp: SparqlEngine[Jena, URL] = new JenaSparqlHttpEngine

  implicit val rdfStore: RDFStore[Jena, Dataset] with SparqlUpdate[Jena, Dataset] = new JenaDatasetStore(true)

  implicit val rdfXMLReader: RDFReader[Jena, RDFXML] = JenaRDFReader.rdfxmlReader()

  implicit val turtleReader: RDFReader[Jena, Turtle] = JenaRDFReader.turtleReader()

  implicit val n3Reader: RDFReader[Jena, N3] = JenaRDFReader.n3Reader()

  implicit val readerSelector: ReaderSelector[Jena] = JenaRDFReader.selector

  implicit val rdfXMLWriter: RDFWriter[Jena, RDFXML] = JenaRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Jena, Turtle] = JenaRDFWriter.turtleWriter

  implicit val n3Writer: RDFWriter[Jena, N3] = JenaRDFWriter.n3Writer

  implicit val writerSelector: RDFWriterSelector[Jena] = JenaRDFWriter.selector

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter.solutionsWriterXml

  implicit val sparqlSolutionsWriterSelector: SparqlSolutionsWriterSelector[Jena] = JenaSolutionsWriter.solutionsWriterSelector

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    JenaQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    JenaQueryResultsReader.queryResultsReaderXml

}
