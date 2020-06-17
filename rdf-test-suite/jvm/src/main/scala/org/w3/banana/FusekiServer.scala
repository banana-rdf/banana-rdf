package org.w3.banana

import org.apache.jena.atlas.lib.FileOps
import org.apache.jena.fuseki.system.FusekiLogging
import org.apache.jena.fuseki.jetty.JettyServerConfig
import org.apache.jena.fuseki.webapp.FusekiEnv
import org.apache.jena.query.Dataset
import org.apache.jena.util.FileManager
import org.apache.jena.fuseki.cmd.JettyFusekiWebapp

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

  lazy val conf = {
    //from https://github.com/apache/jena/blob/master/jena-fuseki2/jena-fuseki-core/src/test/java/org/apache/jena/fuseki/TS_Fuseki.java
    val FusekiTestHome = "target/FusekiHome"
    FileOps.ensureDir(FusekiTestHome)
    FileOps.clearDirectory(FusekiTestHome)
    System.setProperty("FUSEKI_HOME", FusekiTestHome)
    FusekiLogging.setLogging()
    FusekiEnv.setEnvironment()
    // Avoid any persistent record.
    val config = new JettyServerConfig()
    config.port = port
    config.contextPath = path
    config.enableCompression = true
    config.verboseLogging = true
    config
  }

  val server ={
    JettyFusekiWebapp.initializeServer(conf)
    JettyFusekiWebapp.instance
  } //EmbeddedFusekiServer.create(port, tdb, path);

  JettyFusekiWebapp.initializeServer(conf)

  def start() = server.start

  def stop() = server.stop
}