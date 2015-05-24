package org.w3.banana.bigdata

import org.w3.banana._


class BigdataModule  extends RDFModule with RDFOpsModule  with RecordBinderModule //with  SparqlGraphModule
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

}
