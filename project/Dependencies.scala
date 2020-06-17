import sbt._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType, _}
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._


object Dependencies {
  // Note: %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  /**
    * scalaz
    * @see http://scalaz.org
    * @see http://repo1.maven.org/maven2/org/scalaz/
    */
  val scalaz = Def.setting("org.scalaz" %%% "scalaz-core" % "7.3.1")

  /**
   * joda-Time
   * @see http://joda-time.sourceforge.net
   * @see http://repo1.maven.org/maven2/joda-time/joda-time/
   */
  val jodaTime = "joda-time" % "joda-time" % "2.9.6"

  /**
   * joda-convert
   * @see http://joda-convert.sourceforge.net
   * @see http://repo1.maven.org/maven2/org/joda/joda-convert
   */
  val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"

  /**
   * scalatest
   * @see http://www.scalatest.org
   * @see http://repo1.maven.org/maven2/org/scalatest
   */
  val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.1.2")
  
  /**
   * Akka Http Core
   * @see http://akka.io
   * @see http://repo1.maven.org/maven2/com/typesafe/akka
   */
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % "10.0.0"

  /**
   * Apache Commons Logging
   * @see http://commons.apache.org/proper/commons-logging/
   * @see https://repo1.maven.org/maven2/commons-logging/commons-logging
   */
  val commonsLogging = "commons-logging" % "commons-logging" % "1.2"

  /**
   * jena
   * @see https://jena.apache.org/
   * @see http://repo1.maven.org/maven2/org/apache/jena
   */
 val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "3.4.0" 

  /**
   * logback for jena
   * @see http://logging.apache.org/log4j/1.2/
   * @see http://repo1.maven.org/maven2/log4j/log4j/
   */
  val logback = "log4j" % "log4j" % "1.2.16" % "provided"

  /**
   * Aalto
   * @see http://wiki.fasterxml.com/AaltoHome
   * @see http://repo1.maven.org/maven2/com/fasterxml/aalto-xml
   */
  val aalto = "com.fasterxml" % "aalto-xml" % "1.0.0"

  /**
   * RDF4J
   * @see http://www.rdf4j.org/
   * @see https://repo1.maven.org/maven2/org/eclipse/rdf4j/
   */
  val rdf4jVersion = "3.2.2"

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
   * @see http://repo.typesafe.com/typesafe/snapshots/com/github/jsonld-java/jsonld-java-tools
   */
  val jsonldJava = "com.github.jsonld-java" % "jsonld-java" % "0.10.0"

  /**
   * parboiled
   * @see http://parboiled.org
   * @see http://repo1.maven.org/maven2/org/parboiled/
   */
  val parboiled2 = "org.parboiled" %% "parboiled" % "2.1.3"

  /**
   * jena-fuseki
   * @see http://jena.apache.org/documentation/serving_data
   * @see http://repo1.maven.org/maven2/org/apache/jena/jena-fuseki/
   */
  val fusekiVersion =  "3.4.0"
  val fuseki = "org.apache.jena" % "apache-jena-fuseki" % fusekiVersion
  val fusekiServer = "org.apache.jena" % "jena-fuseki-server" % fusekiVersion 
  
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.1.0"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.2"
}
