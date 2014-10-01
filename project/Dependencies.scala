import sbt.Keys._
import sbt.{ExclusionRule, _}
import com.inthenow.sbt.scalajs.SbtScalajs
import com.inthenow.sbt.scalajs.SbtScalajs._
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object Dependencies {

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.6"

  val jodaTime = "joda-time" % "joda-time" % "2.1"

  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.0"

  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core-experimental" % "0.4"

  // jena

  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "2.11.2"

  val logback = "log4j" % "log4j" % "1.2.16" % "provided"

  val aalto = "com.fasterxml" % "aalto-xml" % "0.9.7"

  // sesame

  val sesameVersion = "2.8.0-beta1"
  
  val sesameQueryAlgebra = "org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % sesameVersion
  val sesameQueryParser = "org.openrdf.sesame" % "sesame-queryparser-sparql" % sesameVersion
  val sesameQueryResult = "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % sesameVersion
  val sesameRioTurtle = "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion
  val sesameRioRdfxml =  "org.openrdf.sesame" % "sesame-rio-rdfxml" % sesameVersion
  val sesameSailMemory = "org.openrdf.sesame" % "sesame-sail-memory" % sesameVersion
  val sesameSailNativeRdf = "org.openrdf.sesame" % "sesame-sail-nativerdf" % sesameVersion
  val sesameRepositorySail = "org.openrdf.sesame" % "sesame-repository-sail" % sesameVersion

  // other

  val parboiled2 = "org.parboiled" %% "parboiled" % "2.0.0"

  val jasmine_jvm = "com.github.inthenow" %% "jasmine_jvm" % "0.2.5"

  val scalajsJasmine = "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion

  // %%% can only be used within a task or setting macro, such as :=, +=, ++=, Def.task, or Def.setting...

  val scalaz_js = Seq(libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.0.6")

  val jasmine_js = Seq(libraryDependencies += "com.github.inthenow" %%% "jasmine_js" % "0.2.5")

  val jasmine_jsTest = Seq(libraryDependencies += "com.github.inthenow" %%% "jasmine_js" % "0.2.5" % "test")

}
