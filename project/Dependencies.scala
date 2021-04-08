import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys.scalaVersion
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject, _}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._


object Dependencies {
  // Note: %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  /**
    * scalaz
    * @see https://scalaz.org
    * @see https://repo1.maven.org/maven2/org/scalaz/
    */
   val scalaz = Def.setting("org.scalaz" %%% "scalaz-core" % "7.4.0-M7")

  /**
   * joda-Time
   * @see https://joda-time.sourceforge.net
   * @see https://repo1.maven.org/maven2/joda-time/joda-time/
   */
  val jodaTime = "joda-time" % "joda-time" % "2.9.6"

  /**
   * joda-convert
   * @see https://joda-convert.sourceforge.net
   * @see https://repo1.maven.org/maven2/org/joda/joda-convert
   */
  val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"

  /**
   * scalatest
   * @see https://www.scalatest.org
   * @see https://repo1.maven.org/maven2/org/scalatest
   */
  val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.7")

  /**
   * Akka Http Core
   * @see https://akka.io
   * @see https://repo1.maven.org/maven2/com/typesafe/akka
   */
  val akkaHttpCore = ("com.typesafe.akka" %% "akka-http-core" % "10.2.4") cross CrossVersion.for3Use2_13
  val akka = ("com.typesafe.akka" %% "akka-actor-typed" % "2.6.13") cross CrossVersion.for3Use2_13

  /**
   * jena
   * @see https://jena.apache.org/
   * @see https://repo1.maven.org/maven2/org/apache/jena
   */
  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "3.17.0"

  /**
   * slf4j-nop. Test dependency for logging.
   * @see https://www.slf4j.org
   */
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.7.21" % Test

  /**
   * Aalto
   * @see https://wiki.fasterxml.com/AaltoHome
   * @see https://repo1.maven.org/maven2/com/fasterxml/aalto-xml
   */
  val aalto = "com.fasterxml" % "aalto-xml" % "1.0.0"

  /**
   * RDF4J
   * @see https://www.rdf4j.org/
   * @see https://repo1.maven.org/maven2/org/eclipse/rdf4j/
   */
  val rdf4jVersion = "3.6.1"

  val rdf4jQueryAlgebra = "org.eclipse.rdf4j" % "rdf4j-queryalgebra-evaluation" % rdf4jVersion
  val rdf4jQueryParser = "org.eclipse.rdf4j" % "rdf4j-queryparser-sparql" % rdf4jVersion
  val rdf4jQueryResult = "org.eclipse.rdf4j" % "rdf4j-queryresultio-sparqljson" % rdf4jVersion
  val rdf4jRioTurtle = "org.eclipse.rdf4j" % "rdf4j-rio-turtle" % rdf4jVersion
  val rdf4jRioRdfxml =  "org.eclipse.rdf4j" % "rdf4j-rio-rdfxml" % rdf4jVersion
  val rdf4jRioJsonLd =  "org.eclipse.rdf4j" % "rdf4j-rio-jsonld" % rdf4jVersion
  val rdf4jSailMemory = "org.eclipse.rdf4j" % "rdf4j-sail-memory" % rdf4jVersion
  val rdf4jSailNativeRdf = "org.eclipse.rdf4j" % "rdf4j-sail-nativerdf" % rdf4jVersion
  val rdf4jRepositorySail = "org.eclipse.rdf4j" % "rdf4j-repository-sail" % rdf4jVersion

  /**
   * jsonld-java
   * @see https://github.com/jsonld-java/jsonld-java
   * @see https://repo.typesafe.com/typesafe/snapshots/com/github/jsonld-java/jsonld-java-tools
   */
  val jsonldJava = "com.github.jsonld-java" % "jsonld-java" % "0.13.2"

  /**
   * parboiled
   * @see https://parboiled.org
   * @see https://repo1.maven.org/maven2/org/parboiled/
   */
  val parboiled2 = "org.parboiled" %% "parboiled" % "2.1.3"

  /**
   * jena-fuseki
   * @see https://jena.apache.org/documentation/serving_data
   * @see https://repo1.maven.org/maven2/org/apache/jena/jena-fuseki/
   */
  val fuseki = "org.apache.jena" % "jena-fuseki-main" % "3.17.0"
  
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.1.0"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.3"
}
