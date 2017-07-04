import com.inthenow.sbt.scalajs.SbtScalajs._
import com.inthenow.sbt.scalajs._
import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtunidoc.Plugin._

object BuildSettings {
  import Publishing._
  implicit val logger = ConsoleLogger()

  val buildSettings = jenkinsMavenSettings ++ defaultScalariformSettings ++ Seq(
    organization := "org.w3",
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.11.7", "2.10.6"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    fork := false,
    parallelExecution in Test := false,
    offline := true,
    //TODO: -optimize causes issues for scala.js, so in new build setup should be jvm only
    scalacOptions ++= Seq("-deprecation", "-unchecked", /*"-optimize",*/ "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140", "-Xfatal-warnings"),
    scalacOptions in(Compile, doc) := Seq("-groups", "-implicits"),
    description := "RDF framework for Scala",
    startYear := Some(2012),
    resolvers += Resolver.bintrayRepo("inthenow","releases"),
    updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
  )
}

object BananaRdfBuild extends Build {

  import BuildSettings._
  import Dependencies._
 
  val crossBuildType = CommonBaseBuild

  /** `banana`, the root project. */
  lazy val bananaM  = CrossModule(RootBuild,
    id              = "banana",
    defaultSettings = buildSettings)

  lazy val banana = bananaM
    .project(Module, banana_jvm)
    .settings(unidocSettings:_*)

  lazy val banana_jvm = bananaM
    .project(Jvm, rdf_jvm, rdfTestSuite_jvm, ntriples_jvm, plantain_jvm, jena, sesame, examples)
    .settings(aggregate in Test in rdf_jvm:= false,
              aggregate in Test in rdfTestSuite_jvm := false,
              aggregate in Test in ntriples_jvm := false)

  /** `rdf`, a cross-compiled base module for RDF abstractions. */
  lazy val rdfM = CrossModule(crossBuildType,
    id              = "rdf",
    baseDir         = "rdf",
    defaultSettings = buildSettings ++ Seq(aggregate in Test := false),
    modulePrefix    = "banana-",
    sharedLabel     = "common")

  lazy val rdf     = rdfM.project(Module, rdf_jvm)
  lazy val rdf_jvm = rdfM.project(Jvm, rdf_common_jvm)

  lazy val rdf_common_jvm = rdfM
    .project(Jvm,Shared)
    .settings(
        libraryDependencies += scalaz,
        libraryDependencies += jodaTime,
        libraryDependencies += jodaConvert)

   /** `ntriples`, blocking yet streaming parser */
  lazy val ntriplesM  = CrossModule(crossBuildType,
    id                = "ntriples",
    baseDir           = "io/ntriples",
    defaultSettings   = buildSettings++ Seq(aggregate in Test := false),
    modulePrefix      = "banana-io-",
    sharedLabel       = "common")

  lazy val ntriples     = ntriplesM.project(Module, ntriples_jvm)
  lazy val ntriples_jvm = ntriplesM.project(Jvm, Empty,  ntriples_common_jvm).dependsOn(rdf_jvm)

  lazy val ntriples_common_jvm = ntriplesM
    .project(Jvm,Shared)
    .settings(buildSettings:_*)
    .dependsOn(rdf_jvm)

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
    defaultSettings = buildSettings++ Seq(aggregate in Test := false),
    modulePrefix    = "banana-",
    sharedLabel     = "common"
  )

  lazy val rdfTestSuite = rdfTestSuiteM.project(Module, rdfTestSuite_jvm)

  lazy val rdfTestSuite_jvm = rdfTestSuiteM
    .project(Jvm, rdfTestSuite_common_jvm)
    .settings(Seq(resourceDirectory in Test := baseDirectory.value / "src/main/resources"):_*)
    .dependsOn(rdf_jvm, ntriples_jvm)

  lazy val rdfTestSuite_common_jvm = rdfTestSuiteM
    .project(Jvm, Shared)
    .settings(
      Seq(
        libraryDependencies += scalatest,
        libraryDependencies += jodaTime,
        libraryDependencies += jodaConvert,
        libraryDependencies += fuseki,
        libraryDependencies += servlet,
        libraryDependencies += httpComponents,
        libraryDependencies += httpComponentsCache
      )
    )
    .dependsOn(rdf_jvm)

  /** `jena`, an RDF implementation for Apache Jena. */
  lazy val jenaM = CrossModule(SingleBuild,
    id              = "jena",
    baseDir         = "jena",
    defaultSettings = buildSettings,
    modulePrefix    = "banana-",
    sharedLabel     = "common")

  lazy val jena = jenaM
    .project(Jvm)
    .settings(aggregate in Test in rdf_jvm := false,
             aggregate in Test in ntriples_jvm := false,
              aggregate in Test in rdfTestSuite_jvm := false)
    .settings(
      Seq(
        resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
        libraryDependencies += jenaLibs,
        libraryDependencies += httpComponents,
        libraryDependencies += httpComponentsCache,
        libraryDependencies += commonsLogging,
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
    .settings(aggregate in Test in rdf_jvm := false,
             aggregate in Test in ntriples_jvm := false,
              aggregate in Test in rdfTestSuite_jvm := false)
    .settings(
        libraryDependencies += sesameQueryAlgebra,
        libraryDependencies += sesameQueryParser,
        libraryDependencies += sesameQueryResult,
        libraryDependencies += sesameRioTurtle,
        libraryDependencies += sesameRioRdfxml,
        libraryDependencies += sesameSailMemory,
        libraryDependencies += sesameSailNativeRdf,
        libraryDependencies += sesameRepositorySail,
        libraryDependencies += httpComponents,
        libraryDependencies += commonsLogging,
        libraryDependencies += jsonldJava)
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
    .project(Module, plantain_jvm)

  lazy val plantain_jvm = plantainM
    .project(Jvm, plantain_common_jvm)
    .settings(aggregate in Test in rdf_jvm := false,
             aggregate in Test in ntriples_jvm := false,
              aggregate in Test in rdfTestSuite_jvm := false)
    .settings(
      Seq(
        libraryDependencies += akkaHttpCore,
        libraryDependencies += sesameRioTurtle,
        libraryDependencies += jsonldJava): _*)
    .dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  lazy val plantain_common_jvm = plantainM
    .project(Jvm, Shared)
    .settings(aggregate in Test in rdf_jvm := false,
              aggregate in Test in rdfTestSuite_jvm := false)
    .dependsOn(rdf_jvm, rdfTestSuite_jvm % "test-internal->compile")

  /** `N3.js`, a js only module binding N3.js into banana-rdf abstractions. */
  lazy val n3JsM  = CrossModule(SingleBuild,
    id                = "n3-js",
    baseDir           = "N3.js",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-")

  /** `jsonld.js`, a js only module binding jsonld.js into banana-rdf abstractions. */
  lazy val jsonldJsM  = CrossModule(SingleBuild,
    id                = "jsonld-js",
    baseDir           = "jsonld.js",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-")

  /** `examples`, a bunch of working examples using banana-rdf abstractions.*/
  lazy val examplesM = CrossModule(SingleBuild,
    id              = "examples",
    baseDir         = "misc/examples",
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
