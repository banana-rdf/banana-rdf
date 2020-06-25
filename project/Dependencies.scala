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
   * jena
   * @see https://jena.apache.org/
   * @see http://repo1.maven.org/maven2/org/apache/jena
   */
  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "3.15.0" 

  /**
   * slf4j-nop. Test dependency for logging.
   * @see http://www.slf4j.org
   */
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.7.21" % Test

  /**
   * Aalto
   * @see http://wiki.fasterxml.com/AaltoHome
   * @see http://repo1.maven.org/maven2/com/fasterxml/aalto-xml
   */
  val aalto = "com.fasterxml" % "aalto-xml" % "1.0.0"

  /**
   * sesame
   * @see http://www.openrdf.org/
   * @see https://repo1.maven.org/maven2/org/openrdf/sesame/
   */
  val sesameVersion = "2.9.0"

  val sesameQueryAlgebra = "org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % sesameVersion
  val sesameQueryParser = "org.openrdf.sesame" % "sesame-queryparser-sparql" % sesameVersion
  val sesameQueryResult = "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % sesameVersion
  val sesameRioTurtle = "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion
  val sesameRioRdfxml =  "org.openrdf.sesame" % "sesame-rio-rdfxml" % sesameVersion
  val sesameSailMemory = "org.openrdf.sesame" % "sesame-sail-memory" % sesameVersion
  val sesameSailNativeRdf = "org.openrdf.sesame" % "sesame-sail-nativerdf" % sesameVersion
  val sesameRepositorySail = "org.openrdf.sesame" % "sesame-repository-sail" % sesameVersion

  /**
   * jsonld-java
   * @see https://github.com/jsonld-java/jsonld-java
   * @see http://repo.typesafe.com/typesafe/snapshots/com/github/jsonld-java/jsonld-java-sesame
   */
  val jsonldJava = "com.github.jsonld-java" % "jsonld-java-sesame" % "0.5.1"

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
  val fuseki = "org.apache.jena" % "jena-fuseki-main" % "3.15.0"
  
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.1.0"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.2"
}
