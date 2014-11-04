package org.w3.banana

import com.hp.hpl.jena.query.Dataset
import com.hp.hpl.jena.util.FileManager
import org.apache.jena.fuseki.EmbeddedFusekiServer

/**
 * Embedded Fuseki Server
 *
 * Created by tulio.domingos
 */
class FusekiServer(port:Int = 3030, dataset:Dataset, path:String = "ds", dataFiles:List[String] = List()) {

  val tdb = dataset.asDatasetGraph
  val model = dataset.getDefaultModel

  dataFiles.foreach { file =>
    FileManager.get.readModel(model, file, "N-TRIPLES")
  }

  val server = EmbeddedFusekiServer.create(port, tdb, path);

  def start() = server.start

  def stop() = server.stop
}