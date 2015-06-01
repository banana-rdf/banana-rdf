package org.w3.banana.bigdata

import org.w3.banana._
import org.w3.banana.bigdata.io._
import org.w3.banana.io._

import scala.util.Try


class BigdataModule
  extends RDFModule
  with RDFOpsModule
  with RecordBinderModule
  //with SparqlGraphModule
  //with RDFXMLReaderModule
  with TurtleReaderModule
  with NTriplesReaderModule
  with RDFXMLWriterModule
  with TurtleWriterModule
  with NTriplesWriterModule
  with JsonSolutionsWriterModule
  with XmlSolutionsWriterModule
{

  override type Rdf = Bigdata

  /**
   * Config contains config properties and base URI
   * baseURI is required for many things in bigdata,
   * http://todo.example/ is taken only because it is used in Bigdata banana-module
   */
  implicit val config =  DefaultBigdataConfig

  implicit val ops: RDFOps[Bigdata] = new BigdataOps()(config)

  implicit val recordBinder: binder.RecordBinder[Bigdata] = binder.RecordBinder[Bigdata]

  //implicit val rdfXMLReader: RDFReader[Bigdata, Try, RDFXML] = new BigdataRDFXMLReader

  implicit val turtleReader: RDFReader[Bigdata, Try, Turtle] = new BigdataTurtleReader

  implicit val rdfXMLWriter: RDFWriter[Bigdata, Try, RDFXML] = new BigdataRDFWriter[RDFXML]

  implicit val turtleWriter: RDFWriter[Bigdata, Try, Turtle] = new BigdataRDFWriter[Turtle]

  implicit val ntriplesReader: RDFReader[Bigdata, Try, NTriples] = new NTriplesReader

  implicit val ntriplesWriter: RDFWriter[Bigdata, Try, NTriples] = new NTriplesWriter

  implicit val jsonSolutionsWriter: SparqlSolutionsWriter[Bigdata, SparqlAnswerJson] = new BigdataSolutionsWriterJson

  implicit val xmlSolutionsWriter: SparqlSolutionsWriter[Bigdata, SparqlAnswerXml] = new BigdataSolutionsWriterXml


}
