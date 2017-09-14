package org.w3.banana.jena

import org.apache.jena.query.Dataset
import org.w3.banana._
import org.w3.banana.jena.io._
import org.w3.banana.io._
import scala.concurrent.Future
import scala.util.Try
import scala.concurrent.ExecutionContext.Implicits.global

trait JenaModule
extends RDFModule
with RDFOpsModule
with RecordBinderModule
with SparqlGraphModule
with SparqlHttpModule
with RDFXMLReaderModule
with TurtleReaderModule
with NTriplesReaderModule
with JsonLDReaderModule
with NTriplesWriterModule
with RDFXMLWriterModule
with TurtleWriterModule
with JsonSolutionsWriterModule
with XmlSolutionsWriterModule
with JsonQueryResultsReaderModule
with XmlQueryResultsReaderModule {

  type Rdf = Jena

  implicit val ops: JenaOps = new JenaOps

  implicit val jenaUtil: JenaUtil = new JenaUtil

  implicit val recordBinder: binder.RecordBinder[Jena] = binder.RecordBinder[Jena]

  implicit val sparqlOps: SparqlOps[Jena] = new JenaSparqlOps

  implicit val sparqlGraph: SparqlEngine[Jena, Try, Jena#Graph] = JenaGraphSparqlEngine(ops)

  import java.net.URL
  /**
    * @deprecated see issue [[https://github.com/banana-rdf/banana-rdf/issues/332]]
    */
  @deprecated("see issue https://github.com/banana-rdf/banana-rdf/issues/332", "0.8.x")
  implicit val sparqlHttp: SparqlEngine[Jena, Try, URL] with SparqlUpdate[Jena, Try, URL] = new JenaSparqlHttpEngine

  implicit val rdfStore: RDFStore[Jena, Try, Dataset] with SparqlUpdate[Jena, Try, Dataset] = new JenaDatasetStore(true)

  implicit val rdfXMLReader: RDFReader[Jena, Try, RDFXML] = JenaRDFReader.rdfxmlReader()

  implicit val turtleReader: RDFReader[Jena, Try, Turtle] = JenaRDFReader.turtleReader()

  implicit val ntriplesReader: RDFReader[Jena, Try, NTriples] = new NTriplesReader

  implicit val n3Reader: RDFReader[Jena, Try, N3] = JenaRDFReader.n3Reader()

  implicit val rdfXMLWriter: RDFWriter[Jena, Try, RDFXML] = JenaRDFWriter.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Jena, Try, Turtle] = JenaRDFWriter.turtleWriter

  implicit val n3Writer: RDFWriter[Jena, Try, N3] = JenaRDFWriter.n3Writer

  implicit val ntriplesWriter: RDFWriter[Jena, Try, NTriples] = new NTriplesWriter[Jena]

  implicit val jsonldReader: RDFReader[Rdf, Try, JsonLd] = JenaRDFReader.jsonLdReader

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerJson] =
    JenaSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Jena, SparqlAnswerXml] =
    JenaSolutionsWriter.solutionsWriterXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerJson] =
    JenaQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Jena, SparqlAnswerXml] =
    JenaQueryResultsReader.queryResultsReaderXml

  implicit val jsonldCompactedWriter: RDFWriter[Jena, Try, JsonLdCompacted] =
    JenaRDFWriter.jsonldCompactedWriter

}
