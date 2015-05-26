package org.w3.banana.bigdata

import org.w3.banana._
import org.w3.banana.bigdata.io._
import org.w3.banana.io._

import scala.util.Try


class BigdataModule  extends RDFModule
  with RDFOpsModule
  with RecordBinderModule
  with RDFXMLWriterModule
  with TurtleWriterModule
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
  
  implicit val bigdataRDFWriterHelper = new BigdataRDFWriterHelper

  implicit val rdfXMLWriter: RDFWriter[Bigdata, Try, RDFXML] = bigdataRDFWriterHelper.rdfxmlWriter

  implicit val turtleWriter: RDFWriter[Bigdata, Try, Turtle] = bigdataRDFWriterHelper.turtleWriter
}
