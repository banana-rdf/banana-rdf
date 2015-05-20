import sbt.Keys._
import sbt._

import com.inthenow.sbt.scalajs.SbtScalajs
import com.inthenow.sbt.scalajs.SbtScalajs._
import org.scalajs.sbtplugin._
import ScalaJSPlugin.autoImport._

object Dependencies {
  // Note: %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  /**
    * scalaz
    * @see http://scalaz.org
    * @see http://repo1.maven.org/maven2/org/scalaz/
    */
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.1"

  /**
   * scalaz for scalajs
   * @see http://scalaz.org
   * @see http://repo1.maven.org/maven2/com/github/japgolly/fork/scalaz
   */
  // Todo: %%%! --> %%%
  val scalaz_js = Seq(libraryDependencies += "com.github.japgolly.fork.scalaz" %%%! "scalaz-core" % "7.1.1-2")

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
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"


   /** ScalaCheck
    * @see http://scalacheck.org/
    * @see http://repo1.maven.org/maven2/org/scalacheck/
    */
  //Todo:
  val scalacheck = "com.github.inthenow" %% "scalacheck" % "1.12.2"
  //Todo:
  val scalacheckJs = Seq(libraryDependencies += "com.github.inthenow" %%%! "scalacheck" % "1.12.2")

  val zcheckJs =  Seq(
    libraryDependencies += "com.github.inthenow" %%% "zcheck" % "0.6.2"
  )

  val zcheckJsSettings = Seq(
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %%% "zcheck" % "0.6.2",
    testFrameworks := Seq(new TestFramework("org.scalacheck.ScalaCheckFramework"))
  ) ++ scalacheckJs

  val zcheckJvm = "com.github.inthenow" %% "zcheck" % "0.6.2"

  val zcheckJvmSettings = Seq(
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %% "zcheck" % "0.6.2",
    libraryDependencies += scalacheck,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "5", "-minSuccessfulTests", "33", "-workers", "1", "-verbosity", "1")
  )
  
  /**
   * Akka Http Core
   * @see http://akka.io
   * @see http://repo1.maven.org/maven2/com/typesafe/akka
   */
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core-experimental" % "0.9"

  /**
   * jena
   * @see https://jena.apache.org/
   * @see http://repo1.maven.org/maven2/org/apache/jena
   */
  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "2.13.0"

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
  val sesameVersion = "2.8.3"


  val sesameQueryAlgebra = "org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % sesameVersion
  val sesameQueryParser = "org.openrdf.sesame" % "sesame-queryparser-sparql" % sesameVersion
  val sesameQueryResult = "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % sesameVersion
  val sesameRioTurtle = "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion
  val sesameRioRdfxml =  "org.openrdf.sesame" % "sesame-rio-rdfxml" % sesameVersion
  val sesameSailMemory = "org.openrdf.sesame" % "sesame-sail-memory" % sesameVersion
  val sesameSailNativeRdf = "org.openrdf.sesame" % "sesame-sail-nativerdf" % sesameVersion
  val sesameRepositorySail = "org.openrdf.sesame" % "sesame-repository-sail" % sesameVersion
  //TODO:
  //val sesame
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
   * jena-fuseki
   * @see http://jena.apache.org/documentation/serving_data
   * @see http://repo1.maven.org/maven2/org/apache/jena/jena-fuseki/
   */
  val fuseki = "org.apache.jena" % "jena-fuseki" % "1.1.0" exclude ("org.eclipse.jetty.orbit", "javax.servlet") exclude ("org.apache.httpcomponents", "httpclient")
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.3.2"
}
