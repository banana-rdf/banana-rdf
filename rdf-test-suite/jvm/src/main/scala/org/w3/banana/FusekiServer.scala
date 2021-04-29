package org.w3.banana

import org.apache.jena.atlas.lib.FileOps
import org.apache.jena.fuseki.system.FusekiLogging
import org.apache.jena.fuseki.jetty.JettyServerConfig
import org.apache.jena.query.Dataset
import org.apache.jena.util.FileManager
import org.apache.jena.fuseki.main.{FusekiServer => JenaFusekiServer}
import org.apache.jena.riot.RDFDataMgr


/**
 * Embedded Fuseki Server
 *
 * @param dataset a TDB dataset obtained via TDBFactory.
 * @param port the port where the server object will listen to. Default is 3030
 * @param path dataset path. Default is "ds"
 * @param dataFiles list of RDF file paths.
 */
class FusekiServer(dataset:Dataset, port:Int = 3030, path:String = "/ds", dataFiles:List[String] = List()) {

  val tdb = dataset.asDatasetGraph
  val model = dataset.getDefaultModel

  dataFiles.foreach { file =>
    FileManager.get.readModel(model, file, "N-TRIPLES")
  }

  val server: JenaFusekiServer = JenaFusekiServer.create()
                                                 .port(port)
                                                 .add(path, dataset, true)
                                                 .build() 

  def start() = server.start

  def stop() = server.stop
}