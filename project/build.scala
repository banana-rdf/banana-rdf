import sbt._
import sbt.Keys._
import com.inthenow.sbt.scalajs._
import com.inthenow.sbt.scalajs.SbtScalajs._
import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbtunidoc.Plugin._
import ScalaJSPlugin.autoImport._

object BuildSettings {
  import Publishing._
  implicit val logger = ConsoleLogger()

  val buildSettings = publicationSettings ++ defaultScalariformSettings ++ Seq(
    organization := "org.w3",
    version := "0.8.0-SNAPSHOT",
    scalaVersion := "2.11.5",
    crossScalaVersions := Seq("2.11.5", "2.10.4"),
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    fork := false,
    parallelExecution in Test := false,
    offline := true,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140", "-Yinline-warnings"),
    scalacOptions in(Compile, doc) := Seq("-groups", "-implicits"),
    description := "RDF framework for Scala",
    startYear := Some(2012),
    //Todo:
    resolvers += Resolver.url("inthenow-releases", url("http://dl.bintray.com/inthenow/releases"))(Resolver.ivyStylePatterns)
  )
}

object BananaRdfBuild extends Build {

  import BuildSettings._
  import Dependencies._

  val crossBuildType =  SharedBuild  //SbtLinkedBuild CommonBaseBuild SharedBuild   SymLinkedBuild

  /** `banana`, the root project. */
  lazy val bananaM  = CrossModule(RootBuild,
    id              = "banana",
    defaultSettings = buildSettings)

  lazy val banana = bananaM
    .project(Module, banana_jvm, banana_js)
    .settings(unidocSettings:_*)

  lazy val banana_jvm = bananaM
    .project(Jvm, rdf_jvm, rdfTestSuite_jvm, ntriples_jvm, plantain_jvm, jena, sesame, examples)
    .settings(zcheckJvmSettings:_*)

  lazy val banana_js = bananaM
    .project(Js, rdf_js, rdfTestSuite_js, plantain_js, n3Js, jsonldJs)
    .settings(zcheckJsSettings:_*)

  /** `rdf`, a cross-compiled base module for RDF abstractions. */
  lazy val rdfM = CrossModule(crossBuildType,
    id              = "rdf",
    baseDir         = "rdf",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-",
    sharedLabel     = "common")

  lazy val rdf     = rdfM.project(Module, rdf_jvm, rdf_js)
  lazy val rdf_jvm = rdfM.project(Jvm, rdf_common_jvm)
  lazy val rdf_js  = rdfM.project(Js, rdf_common_js)

  lazy val rdf_common_jvm = rdfM
    .project(Jvm,Shared)
    .settings(
      Seq(
        libraryDependencies += scalaz,
        libraryDependencies += jodaTime,
        libraryDependencies += jodaConvert): _*)

  lazy val rdf_common_js = rdfM
    .project(Js,Shared)
    .settings(scalaz_js: _*)

  /** `ntriples`, blocking yet streaming parser */
  lazy val ntriplesM  = CrossModule(crossBuildType,
    id                = "ntriples",
    baseDir           = "io/ntriples",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-io-",
    sharedLabel       = "common")

  lazy val ntriples     = ntriplesM.project(Module, ntriples_jvm, ntriples_js)
  lazy val ntriples_jvm = ntriplesM.project(Jvm, Empty,  ntriples_common_jvm)
  lazy val ntriples_js = ntriplesM.project(Js,Empty,  ntriples_common_js)
  lazy val ntriples_common_jvm = ntriplesM
    .project(Jvm,Shared)
    .dependsOn(rdf_jvm)

  lazy val ntriples_common_js = ntriplesM
    .project(Js,Shared)
    .dependsOn(rdf_js)

  /** `ldpatch`, an implementation for LD Patch. See http://www.w3.org/TR/ldpatch/ .*/
  lazy val ldpatchM   = CrossModule(SingleBuild,
    id                = "ldpatch",
    baseDir           = "ldpatch",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-",
    sharedLabel       = "common")

  lazy val ldpatch = ldpatchM
    .project(Jvm)
    .settings(
      Seq(
        libraryDependencies += parboiled2,
        libraryDependencies += scalatest % "test") ++ XScalaMacroDependencies: _*)
    .dependsOn(rdf_jvm, jena, rdfTestSuite_jvm % "test-internal->compile")

  /** `rdf-test-suite`, a cross-compiled test suite for RDF. */
  lazy val rdfTestSuiteM = CrossModule(crossBuildType,
    id              = "rdf-test-suite",
    baseDir         = "rdf-test-suite",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-",
    sharedLabel     = "common"
  )

  lazy val rdfTestSuite = rdfTestSuiteM.project(Module, rdfTestSuite_jvm , rdfTestSuite_js)

  lazy val rdfTestSuite_jvm = rdfTestSuiteM
    .project(Jvm, rdfTestSuite_common_jvm)
    .settings(Seq(resourceDirectory in Test := baseDirectory.value / "src/main/resources"):_*)
    .dependsOn(rdf_jvm)

  lazy val rdfTestSuite_js = rdfTestSuiteM
    .project(Js, rdfTestSuite_common_js)
    .settings(Seq(resourceDirectory in Test := baseDirectory.value / "src/main/resources"): _*)
    .dependsOn(rdf_js)

  lazy val rdfTestSuite_common_jvm = rdfTestSuiteM
    .project(Jvm, Shared)
    .settings(
      Seq(
        //resolvers += sonatypeRepo,
        libraryDependencies += scalatest,
        libraryDependencies += jodaTime,
        libraryDependencies += jodaConvert,
        libraryDependencies += fuseki,
        libraryDependencies += servlet,
        libraryDependencies += httpComponents
      ) ++ zcheckJvmSettings: _*)
    .dependsOn(rdf_jvm, ntriples_jvm)

  lazy val rdfTestSuite_common_js = rdfTestSuiteM
    .project(Js, Shared)
    .settings(zcheckJsSettings: _*)
    .dependsOn(rdf_js)

  /** `jena`, an RDF implementation for Apache Jena. */
  lazy val jenaM = CrossModule(SingleBuild,
    id              = "jena",
    baseDir         = "jena",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-",
    sharedLabel     = "common")

  lazy val jena = jenaM
    .project(Jvm)
    .settings(
      Seq(
        resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
        libraryDependencies += jenaLibs,
        libraryDependencies += logback,
        libraryDependencies += aalto): _*)
    .dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  /** `sesame`, an RDF implementation for Sesame. */
  lazy val sesameM = CrossModule(SingleBuild,
    id              = "sesame",
    baseDir         = "sesame",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-")

  lazy val sesame = sesameM
    .project(Jvm)
    .settings(
      Seq(
        libraryDependencies += sesameQueryAlgebra,
        libraryDependencies += sesameQueryParser,
        libraryDependencies += sesameQueryResult,
        libraryDependencies += sesameRioTurtle,
        libraryDependencies += sesameRioRdfxml,
        libraryDependencies += sesameSailMemory,
        libraryDependencies += sesameSailNativeRdf,
        libraryDependencies += sesameRepositorySail,
        libraryDependencies += jsonldJava): _*)
    .dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  /** `plantain`, a cross-compiled Scala implementation for RDF.  */
  lazy val plantainM = CrossModule(crossBuildType,
    id              = "plantain",
    baseDir         = "plantain",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-",
    sharedLabel     = "common"
  )

  lazy val plantain = plantainM
    .project(Module, plantain_jvm, plantain_js)
    .dependsOn(rdfTestSuite)

  lazy val plantain_jvm = plantainM
    .project(Jvm, plantain_common_jvm)
    .settings(
      Seq(
        libraryDependencies += akkaHttpCore,
        libraryDependencies += sesameRioTurtle,
        libraryDependencies += jsonldJava): _*)
    .dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  lazy val plantain_js = plantainM
    .project(Js, plantain_common_js)
    .settings(
      Seq(
        //scalaJSStage in Test := FastOptStage
      ) ++ zcheckJsSettings: _*
    )
    .dependsOn(rdf_js, ntriples_js, rdfTestSuite_js % "test-internal->compile")

  lazy val plantain_common_jvm = plantainM
    .project(Jvm, Shared)
    .dependsOn(rdf_jvm, rdfTestSuite_jvm % "test-internal->compile")

  lazy val plantain_common_js  = plantainM
    .project(Js, Shared)
    .settings(scalaz_js ++ zcheckJsSettings:_*)
    .dependsOn(rdf_js , rdfTestSuite_js % "test-internal->compile")

  /** `N3.js`, a js only module binding N3.js into banana-rdf abstractions. */
  lazy val n3JsM  = CrossModule(SingleBuild,
    id                = "n3-js",
    baseDir           = "N3.js",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-")

  lazy val n3Js = n3JsM
    .project(Js)
    .settings(
      Seq(
        //scalaJSStage in Test := FastOptStage,
        jsDependencies += "org.webjars" % "N3.js" % "9a8de1fc6c"/ "n3-browser.min.js" commonJSName "N3",
        skip in packageJSDependencies := false
      ) ++ zcheckJsSettings : _*
    )
    .dependsOn(rdf_js, plantain_common_js, rdfTestSuite_js % "test-internal->compile", plantain_js % "test-internal->compile")

  /** `jsonld.js`, a js only module binding jsonld.js into banana-rdf abstractions. */
  lazy val jsonldJsM  = CrossModule(SingleBuild,
    id                = "jsonld-js",
    baseDir           = "jsonld.js",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-")

  lazy val jsonldJs = jsonldJsM
    .project(Js)
    .settings(
      Seq(
        scalaJSStage in Test := FastOptStage,
        jsDependencies += ProvidedJS / "jsonld.js" commonJSName "jsonld",
        skip in packageJSDependencies := false
      ) ++ zcheckJsSettings : _*
    )
    .dependsOn(rdf_js, rdfTestSuite_js % "test-internal->compile", plantain_js % "test-internal->compile")

  /** `examples`, a bunch of working examples using banana-rdf abstractions. */
  lazy val examplesM = CrossModule(SingleBuild,
    id              = "examples",
    baseDir         = ".examples",
    defaultSettings = buildSettings
  )

  lazy val examples = examplesM.project(Module, sesame, jena)

  /** A virtual module for gathering experimental ones. */
  lazy val experimentalM = CrossModule(SingleBuild,
    id              = "experimental",
    baseDir         = ".experimental",
    defaultSettings = buildSettings
  )

  lazy val experimental = experimentalM.project(Module, ldpatch)

}
