import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object Dependencies {
  // Note: %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  def withJenaExcludes(module: ModuleID): ModuleID = {
    module excludeAll(
      ExclusionRule("org.eclipse.jetty.orbit", "javax.servlet"),
      ExclusionRule("org.apache.httpcomponents", "httpclient"),
      ExclusionRule("org.apache.httpcomponents", "httpclient-cache")
      )
  }

  def withSesameExcludes(module: ModuleID): ModuleID = {
    module excludeAll(
      ExclusionRule("org.apache.httpcomponents", "httpclient"),
      ExclusionRule("org.apache.httpcomponents", "httpclient-cache")
      )
  }

  /**
    * scalaz
    *
    * @see http://scalaz.org
    * @see http://repo1.maven.org/maven2/org/scalaz/
    */
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.14"

  /**
   * joda-Time
    *
    * @see http://joda-time.sourceforge.net
   * @see http://repo1.maven.org/maven2/joda-time/joda-time/
   */
  val jodaTime = "joda-time" % "joda-time" % "2.9.1"

  /**
   * joda-convert
    *
    * @see http://joda-convert.sourceforge.net
   * @see http://repo1.maven.org/maven2/org/joda/joda-convert
   */
  val jodaConvert = "org.joda" % "joda-convert" % "1.8"

  /**
   * scalatest
    *
    * @see http://www.scalatest.org
   * @see http://repo1.maven.org/maven2/org/scalatest
   */
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1"


   /** ScalaCheck
     *
     * @see http://scalacheck.org/
    * @see http://repo1.maven.org/maven2/org/scalacheck/
    */
  //Todo:
//  val scalacheck = "com.github.inthenow" %% "scalacheck" % "1.12.2"

//  val zcheckJvm = "com.github.inthenow" %% "zcheck" % "0.6.2"

/*
  val zcheckJvmSettings = Seq(
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns),
    libraryDependencies += "com.github.inthenow" %% "zcheck" % "0.6.2",
    //libraryDependencies += scalacheck,
    testOptions in Test += Tests.Argument(TestFrameworks.ScalaCheck, "-maxSize", "5", "-minSuccessfulTests", "33", "-workers", "1", "-verbosity", "1")
  )
*/

  /**
   * Akka Http Core
    *
    * @see http://akka.io
   * @see http://repo1.maven.org/maven2/com/typesafe/akka
   */
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % "10.0.6"

  /**
   * Apache Commons Logging
    *
    * @see http://commons.apache.org/proper/commons-logging/
   * @see https://repo1.maven.org/maven2/commons-logging/commons-logging
   */
  val commonsLogging = "commons-logging" % "commons-logging" % "1.2"

  /**
   * jena
    *
    * @see https://jena.apache.org/
   * @see http://repo1.maven.org/maven2/org/apache/jena
   */
  val jenaLibs = withJenaExcludes("org.apache.jena" % "apache-jena-libs" % "3.0.0")

  /**
   * logback for jena
    *
    * @see http://logging.apache.org/log4j/1.2/
   * @see http://repo1.maven.org/maven2/log4j/log4j/
   */
  val logback = "log4j" % "log4j" % "1.2.16" % "provided"

  /**
   * Aalto
    *
    * @see http://wiki.fasterxml.com/AaltoHome
   * @see http://repo1.maven.org/maven2/com/fasterxml/aalto-xml
   */
  val aalto = "com.fasterxml" % "aalto-xml" % "1.0.0"

  /**
   * sesame
    *
    * @see http://www.openrdf.org/
   * @see http://repo1.maven.org/maven2/org/openrdf/sesame/
   */
  val sesameVersion = "2.8.7"

  val sesameQueryAlgebra = withSesameExcludes("org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % sesameVersion)
  val sesameQueryParser = withSesameExcludes("org.openrdf.sesame" % "sesame-queryparser-sparql" % sesameVersion)
  val sesameQueryResult = withSesameExcludes("org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % sesameVersion)
  val sesameRioTurtle = withSesameExcludes("org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion)
  val sesameRioRdfxml = withSesameExcludes("org.openrdf.sesame" % "sesame-rio-rdfxml" % sesameVersion)
  val sesameSailMemory = withSesameExcludes("org.openrdf.sesame" % "sesame-sail-memory" % sesameVersion)
  val sesameSailNativeRdf = withSesameExcludes("org.openrdf.sesame" % "sesame-sail-nativerdf" % sesameVersion)
  val sesameRepositorySail = withSesameExcludes("org.openrdf.sesame" % "sesame-repository-sail" % sesameVersion)
  //TODO:
  //val sesame
  /**
   * jsonld-java
    *
    * @see https://github.com/jsonld-java/jsonld-java
   * @see http://repo.typesafe.com/typesafe/snapshots/com/github/jsonld-java/jsonld-java-sesame
   */
  val jsonldJava = withSesameExcludes("com.github.jsonld-java" % "jsonld-java-sesame" % "0.5.0")

  /**
   * parboiled
    *
    * @see http://parboiled.org
   * @see http://repo1.maven.org/maven2/org/parboiled/
   */
  val parboiled2 = "org.parboiled" %% "parboiled" % "2.1.4"

  /**
   * jena-fuseki
    *
    * @see http://jena.apache.org/documentation/serving_data
   * @see http://repo1.maven.org/maven2/org/apache/jena/jena-fuseki/
   */
  val fuseki = withJenaExcludes("org.apache.jena" % "jena-fuseki1" % "1.3.0")
  val servlet = "javax.servlet" % "javax.servlet-api" % "3.0.1"
  val httpComponents = "org.apache.httpcomponents" % "httpclient" % "4.5.1"

  /**
    * httpclient-cache is used by Jena but they're pulling in an older version which conflicts with the version of
    * httpclient that we're pulling in above.
    */
  val httpComponentsCache = "org.apache.httpcomponents" % "httpclient-cache" % "4.5.1"
}
