package org.w3.banana

import java.nio.file.Paths

import org.apache.jena.atlas.lib.FileOps
import org.apache.jena.fuseki.Fuseki
import org.apache.jena.fuseki.jetty.{JettyFuseki, JettyServerConfig}
import org.apache.jena.fuseki.server.{DataAccessPointRegistry, FusekiEnv, FusekiServer, FusekiServerListener, ServerInitialConfig, SystemState}
import org.apache.jena.sparql.core.DatasetGraphFactory
import org.apache.jena.sparql.modify.request.{Target, UpdateDrop}
import org.apache.jena.tdb.base.file.Location
import org.apache.jena.update.UpdateExecutionFactory
import scala.collection.JavaConverters._

/**
 * Embedded Fuseki Server
 *
 * Manage a server for testing. Example for one server per test suite:
 *
 * <pre>
 *     \@BeforeClass def beforeClass() { ServerTest.allocServer() }
 *     \@AfterClass  def afterClass()  { ServerTest.freeServer() }
 *     \@Before      def beforeTest()  { ServerTest.resetServer() }
 * </pre>
 */
class FusekiTestServer(
  port: Int = 3030,
  datasetPath: String = "/dataset",
  updateable: Boolean = true
) {

  val urlRoot       = s"http://localhost:$port/"
  val urlDataset    = s"http://localhost:$port$datasetPath"
  val serviceUpdate = urlDataset + "/update"
  val serviceQuery  = urlDataset + "/query"
  val serviceGSP    = urlDataset + "/data"

  var jetty: Option[JettyFuseki] = None

  def start() = jetty.foreach(_.start)

  def stop() = jetty.foreach(_.stop)

  private def setupServer() {

    SystemState.location = Location.mem()
    SystemState.init$()

    FusekiServerListener.initialSetup = serverInitialConfig()

    JettyFuseki.initializeServer(jettyConfig())

    jetty = Some(JettyFuseki.instance)

    start()
  }

  private def serverInitialConfig(): ServerInitialConfig = {

    val params = new ServerInitialConfig()

    params.dsg = DatasetGraphFactory.createMem()
    params.datasetPath = datasetPath
    params.allowUpdate = updateable

    params
  }

  private def jettyConfig(): JettyServerConfig = {

    val config = new JettyServerConfig()

    // Avoid any persistent record.
    config.port = port
    config.contextPath = "/"
    config.loopback = true
    config.jettyConfigFile = null
    config.pages = Fuseki.PagesStatic
    config.enableCompression = true
    config.verboseLogging = false

    config
  }

  def resetServer() {
    val clearRequest = new UpdateDrop(Target.ALL) ;
    val proc = UpdateExecutionFactory.createRemote(clearRequest, serviceUpdate) ;
    proc.execute() ;
  }
}

object FusekiTestServer {

  val defaultPort = 3030
  val defaultDatasetPath = "/dataset"
  val defaultUpdatable = true
  val fusekiTestHome = "target/FusekiHome"
  val fusekiTestBase = s"$fusekiTestHome/run"

  var server: Option[FusekiTestServer] = None

  def allocServer(): Unit = allocServer()

  def allocServer(
    port: Int = defaultPort,
    datasetPath: String = defaultDatasetPath,
    updateable: Boolean = defaultUpdatable
  ): Unit = if (server.isEmpty) apply(port, datasetPath, updateable)

  def freeServer(): Unit = if (server.isDefined) teardownServer()

  private def teardownServer(): Unit = {
    server.foreach(_.stop())
    server = None

    // Clear out the registry.
    val registry = DataAccessPointRegistry.get
    for (key <- registry.keys.asScala) {
      registry.remove(key)
    }

    // Clear configuration directory.
    FileOps.clearAll(FusekiServer.dirConfiguration.toFile)
  }

  def apply(
    port: Int = defaultPort,
    datasetPath: String = defaultDatasetPath,
    updateable: Boolean = defaultUpdatable
  ): FusekiTestServer = {

    FusekiEnv.FUSEKI_HOME = Paths.get(fusekiTestHome).toAbsolutePath
    FileOps.ensureDir("target")
    FileOps.ensureDir(fusekiTestHome)
    FileOps.ensureDir(fusekiTestBase)
    FusekiEnv.FUSEKI_BASE = Paths.get(fusekiTestBase).toAbsolutePath

    server = Some(new FusekiTestServer(port, datasetPath, updateable))

    server.get
  }
}
