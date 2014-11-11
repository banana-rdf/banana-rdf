package org.w3.banana

import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.util.FileManager
import org.apache.jena.fuseki.EmbeddedFusekiServer

/**
 * Embedded Fuseki Server
 *
 * @param dataset a TDB dataset obtained via TDBFactory.
 * @param port the port where the server object will listen to. Default is 3030
 * @param path dataset path. Default is "ds"
 * @param dataFiles list of RDF file paths.
 */
class FusekiServer(dataset:Dataset, port:Int = 3030, path:String = "ds", dataFiles:List[String] = List()) {

  val tdb = dataset.asDatasetGraph
  val model = dataset.getDefaultModel

  dataFiles.foreach { file =>
    FileManager.get.readModel(model, file, "N-TRIPLES")
  }

  val server = EmbeddedFusekiServer.create(port, tdb, path);

  def start() = server.start

  def stop() = server.stop
}