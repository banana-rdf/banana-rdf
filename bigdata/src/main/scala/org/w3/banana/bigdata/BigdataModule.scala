package org.w3.banana.bigdata

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import org.w3.banana._
import org.w3.banana.bigdata.io._
import org.w3.banana.io._

import scala.util.Try


class BigdataModule
  extends RDFModule
  with RDFOpsModule
  with RecordBinderModule
  with SparqlGraphModule
  with RDFXMLReaderModule
  with TurtleReaderModule
  with NTriplesReaderModule
  with RDFXMLWriterModule
  with TurtleWriterModule
  with NTriplesWriterModule
  with JsonSolutionsWriterModule
  with XmlSolutionsWriterModule
{

  override type Rdf = Bigdata

  implicit val ops: RDFOps[Bigdata] = new BigdataOps()

  implicit val recordBinder: binder.RecordBinder[Bigdata] = new BigdataBinder

  implicit val sparqlGraph: SparqlEngine[Bigdata, Try, Bigdata#Graph] = new BigdataGraphSparqlEngine

  implicit val sparqlOps:SparqlOps[Bigdata] = new BigdataSparqOps
  
  implicit val rdfStore: RDFStore[Bigdata, Try, BigdataSailRepositoryConnection] = new BigdataStore

  implicit val rdfXMLReader: RDFReader[Bigdata, Try, RDFXML] = new BigdataRDFXMLReader

  implicit val turtleReader: RDFReader[Bigdata, Try, Turtle] = new BigdataTurtleReader

  implicit val ntriplesReader: RDFReader[Bigdata, Try, NTriples] = new NTriplesReader

  implicit val rdfXMLWriter: RDFWriter[Bigdata, Try, RDFXML] = new BigdataRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Bigdata, Try, Turtle] = new BigdataRDFWriter[Turtle]

  implicit val ntriplesWriter: RDFWriter[Bigdata, Try, NTriples] = new NTriplesWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Bigdata, SparqlAnswerJson] = BigdataSolutionsWriter.solutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Bigdata,SparqlAnswerXml]  = BigdataSolutionsWriter.solutionsWriterXml

  implicit val jsonQueryResultsReader: SparqlQueryResultsReader[Bigdata, SparqlAnswerJson] = BigdataQueryResultsReader.queryResultsReaderJson

  implicit val xmlQueryResultsReader: SparqlQueryResultsReader[Bigdata, SparqlAnswerXml] = BigdataQueryResultsReader.queryResultsReaderXml

}
