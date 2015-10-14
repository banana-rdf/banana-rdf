import sbt.Keys._
import sbt._

import scala.scalajs.sbtplugin.ScalaJSPlugin._

object Dependencies {
  // Note: %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  /**
    * scalaz
    * @see http://scalaz.org
    * @see http://repo1.maven.org/maven2/org/scalaz/
    */
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.6"

  /**
   * scalaz for scalajs
   * @see http://scalaz.org
   * @see http://repo1.maven.org/maven2/com/github/japgolly/fork/scalaz
   */
  val scalaz_js = Seq(libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.0.6")

  /**
   * joda-Time
   * @see http://joda-time.sourceforge.net
   * @see http://repo1.maven.org/maven2/joda-time/joda-time/
   */
  val jodaTime = "joda-time" % "joda-time" % "2.1"

  /**
   * joda-convert
   * @see http://joda-convert.sourceforge.net
   * @see http://repo1.maven.org/maven2/org/joda/joda-convert
   */
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  /**
   * scalatest
   * @see http://www.scalatest.org
   * @see http://repo1.maven.org/maven2/org/scalatest
   */
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.0"

  /**
   * Akka Http Core
   * @see http://akka.io
   * @see http://repo1.maven.org/maven2/com/typesafe/akka
   */
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0"

  /**
   * jena
   * @see https://jena.apache.org/
   * @see http://repo1.maven.org/maven2/org/apache/jena
   */
  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "2.12.1"

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
  val aalto = "com.fasterxml" % "aalto-xml" % "0.9.7"

  /**
   * sesame
   * @see http://www.openrdf.org/
   * @see http://repo1.maven.org/maven2/org/openrdf/sesame/
   */
  val sesameVersion = "2.8.0-beta2"
  
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
  val jsonldJava = "com.github.jsonld-java" % "jsonld-java-sesame" % "0.5.0"

  /**
   * parboiled
   * @see http://parboiled.org
   * @see http://repo1.maven.org/maven2/org/parboiled/
   */
  val parboiled2 = "org.parboiled" %% "parboiled" % "2.0.0"

  /**
   * Jasmine js and jvm
   * @see https://github.com/InTheNow
   * @see https://oss.sonatype.org/content/repositories/releases/com/github/inthenow/
   */
  val jasmine_version = "0.3.1"
  val jasmine_jvm = "com.github.inthenow" %% "jasmine_jvm" % jasmine_version
  val jasmine_js = Seq(libraryDependencies += "com.github.inthenow" %%% "jasmine_js" % jasmine_version)
  val jasmine_jsTest = Seq(libraryDependencies += "com.github.inthenow" %%% "jasmine_js" % jasmine_version % "test")

  /**
   * scalajs-jasmine-test-framework
   * @see http://scala-js.org
   * @see http://dl.bintray.com/content/scala-js/scala-js-releases/org.scala-lang.modules.scalajs/scalajs-jasmine-test-framework_2.11
   */
  val scalajsJasmine = "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion

  /**
   * jena-fuseki
   * @see http://jena.apache.org/documentation/serving_data
   * @see http://repo1.maven.org/maven2/org/apache/jena/jena-fuseki/
   */
  val fuseki = "org.apache.jena" % "jena-fuseki" % "1.1.0" exclude ("org.eclipse.jetty.orbit", "javax.servlet") exclude ("org.apache.httpcomponents", "httpclient")
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.3.2"
}
