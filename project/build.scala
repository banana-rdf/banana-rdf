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
    startYear := Some(2012)
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
    .project(Js, rdf_js, /**** rdfTestSuite_js, ****/ plantain_js)
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

  lazy val rdfTestSuite = rdfTestSuiteM.project(Module, rdfTestSuite_jvm)//////////, rdfTestSuite_js) :Todo

  lazy val rdfTestSuite_jvm = rdfTestSuiteM
    .project(Jvm, rdfTestSuite_common_jvm)
    .settings(Seq(resourceDirectory in Test := baseDirectory.value / "src/main/resources"):_*)
    .dependsOn(rdf_jvm)
/****
  lazy val rdfTestSuite_js = rdfTestSuiteM
    .project(Js, rdfTestSuite_common_js)
    .settings(Seq(resourceDirectory in Test := baseDirectory.value / "src/main/resources"): _*)
    .dependsOn(rdf_js)
****/
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
/****
  lazy val rdfTestSuite_common_js = rdfTestSuiteM
    .project(JsShared)
    .settings(zcheckJsSettings: _*)
    .dependsOn(rdf_js)
  ****/
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
    .settings(zcheckJsSettings:_*)
    .dependsOn(rdf_js, ntriples_js) ///////////////, rdfTestSuite_js % "test-internal->compile")

  lazy val plantain_common_jvm = plantainM
    .project(Jvm, Shared)
    .dependsOn(rdf_jvm, rdfTestSuite_jvm % "test-internal->compile")

  lazy val plantain_common_js  = plantainM
    .project(Js, Shared)
    .settings(scalaz_js ++ zcheckJsSettings:_*)
    .dependsOn(rdf_js) //////////:Todo  , rdfTestSuite_js % "test-internal->compile")

  /** `rdfstorew`, a js only module binding rdfstore-js into banana-rdf abstractions. */
  lazy val rdfstorewM  = CrossModule(SingleBuild,
    id                = "rdfstorew",
    baseDir           = "rdfstorew",
    defaultSettings   = buildSettings,
    modulePrefix      = "banana-")

  lazy val rdfstorew = rdfstorewM
    .project(Js)
    .settings(
      Seq(
        jsDependencies += ProvidedJS / "rdf_store.js",
        jsDependencies += "org.webjars" % "momentjs" % "2.7.0" / "moment.js",
        skip in packageJSDependencies := false): _*)
    .dependsOn(rdf_js, jena)//////////////, rdfTestSuite_js % "test-internal->compile")

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

/*

  /** `banana`, the root project. */
  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ unidocSettings
  )//.dependsOn(banana_js, banana_jvm)
   .aggregate(banana_js, banana_jvm)

  /** `banana_js`, a js only meta project. */
  lazy val banana_js = Project(
    id = "banana_js",
    base = file(".banana_js"),
    settings = buildSettings ++ Seq(
      aggregate in Test in rdf_js := false,
      aggregate in Test in rdfTestSuite_js := false
    ) ++ zcheckJsSettings
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdfTestSuite_js, plantain_js)
   .aggregate(rdf_js, rdfTestSuite_js, plantain_js)

  /** `banana_jvm`, a jvm only meta project. */
  lazy val banana_jvm = Project(
    id = "banana_jvm",
    base = file(".banana_jvm"),
    settings = buildSettings ++ Seq (
      aggregate in Test in rdf_jvm := false,
      aggregate in Test in rdfTestSuite_jvm := false
    )++ zcheckJvmSettings
  ).dependsOn(rdf_jvm, rdfTestSuite_jvm, jena, sesame, ntriples_jvm, plantain_jvm, examples)
   .aggregate(rdf_jvm, rdfTestSuite_jvm, jena, sesame, ntriples_jvm, plantain_jvm, examples)

  /** A virtual module for gathering experimental ones. */
  lazy val experimental = Project(
    id = "experimental",
    base = file(".experimental"),
    settings = buildSettings,
    aggregate = Seq(ldpatch)
  )

  /** `rdf`, a cross-compiled base module for RDF abstractions.
    *
    * It is composed of 3 source modules + two non-source modules.
    */
  lazy val rdf = Project(
    id = "rdf",
    base = file("rdf"),
    settings = buildSettings ++
      Seq(
        aggregate in Test := false,
        publishMavenStyle := true
      )
  ).dependsOn(rdf_jvm, rdf_js)
    .aggregate(rdf_jvm, rdf_js)

  lazy val rdf_jvm = Project(
    id = "rdf_jvm",
    base = file("rdf/jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).dependsOn(rdf_common_jvm).aggregate(rdf_common_jvm)

  lazy val rdf_common_jvm = Project(
    id = "rdf_common_jvm",
    base = file("rdf/common"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      publishMavenStyle := true
    )
  )

  lazy val rdf_js = Project(
    id = "rdf_js",
    base = file("rdf/js"),
    settings = buildSettings ++ sjsDeps ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_common_js).aggregate(rdf_common_js)

  lazy val rdf_common_js = Project(
    id = "rdf_common_js",
    base = file("rdf/.common_js"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ linkedSources(rdf_common_jvm) ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)

  /**
   * `ntriples`, blocking yet streaming parser
   *
   */
  lazy val ntriples = Project(
    id = "ntriples",
    base = file("io/ntriples"),
    settings = buildSettings ++
      Seq(
        aggregate in Test := false,
        publishMavenStyle := true
      )
  ).dependsOn(ntriples_common_jvm, ntriples_jvm)
    .aggregate(ntriples_jvm)
    .dependsOn(ntriples_common_js, ntriples_js)
    .aggregate(ntriples_js)

  lazy val ntriples_jvm = Project(
    id = "ntriples_jvm",
    base = file("io/ntriples/jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).dependsOn(ntriples_common_jvm).aggregate(ntriples_common_jvm)

  lazy val ntriples_common_jvm = Project(
    id = "ntriples_common_jvm",
    base = file("io/ntriples/common"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      publishMavenStyle := true
    )
  ).dependsOn(rdf_jvm)

//  not doing the js part yet
  lazy val ntriples_js = Project(
    id = "ntriples_js",
    base = file("io/ntriples/js"),
    settings = buildSettings ++ sjsDeps++ linkedSources(ntriples_jvm) ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs).dependsOn(ntriples_common_js).aggregate(ntriples_common_js)

  lazy val ntriples_common_js = Project(
    id = "ntriples_common_js",
    base = file("io/ntriples/.common_js"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ linkedSources(ntriples_common_jvm) ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js)


  /** `ldpatch`, an implementation for LD Patch.
    *
    * See http://www.w3.org/TR/ldpatch/ .
    */
  lazy val ldpatch = Project(
    id = "ldpatch",
    base = file("ldpatch"),
    settings = buildSettings ++ Seq(
      publishMavenStyle := true,
      libraryDependencies += parboiled2,
      // this will be needed until parboiled 2.0.1 gets released
      // see https://github.com/sirthias/parboiled2/issues/84#
      libraryDependencies <++= scalaVersion {
        case "2.11.4" => Seq("org.scala-lang" % "scala-reflect" % "2.11.4")
        case _ => Seq.empty
      },
      libraryDependencies += scalatest % "test"
    )
  ) dependsOn(rdf_jvm, jena, rdfTestSuite_jvm % "test-internal->compile")

  /* RDF Test Suite */

  /** `rdf-test-suite`, a cross-compiled test suite for RDF.
    *
    * It is composed of 3 source modules + two non-source modules.
    */
  lazy val rdfTestSuite = Project(
    id = "rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).dependsOn(
    rdfTestSuite_jvm,
    rdfTestSuite_common_jvm,
    rdfTestSuite_js,
    rdfTestSuite_common_js
  ).aggregate(
    rdfTestSuite_jvm,
    rdfTestSuite_js
  )

  lazy val rdfTestSuite_jvm = Project(
    id = "rdf-test-suite_jvm",
    base = file("rdf-test-suite/jvm"),
    settings = buildSettings ++ Seq(
      resourceDirectory in Test := baseDirectory.value / "src/main/resources",
      aggregate in Test := false
    )
  ).dependsOn(
    rdf_jvm,
    rdfTestSuite_common_jvm
  ).aggregate(rdfTestSuite_common_jvm)

  lazy val rdfTestSuite_js = Project(
    id = "rdf-test-suite_js",
    base = file("rdf-test-suite/js"),
    settings = buildSettings ++ Seq(
      resourceDirectory in Test := baseDirectory.value / "src/main/resources",
      aggregate in Test := false
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdfTestSuite_common_js)
    .aggregate(rdfTestSuite_common_js)

  lazy val rdfTestSuite_common_jvm = Project(
    id = "rdf-test-suite_common_jvm",
    base = file("rdf-test-suite/common"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      resolvers += sonatypeRepo,
      libraryDependencies += scalatest,
//      libraryDependencies += scalacheck,
     // libraryDependencies += jasmine_jvm,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      libraryDependencies += fuseki,
      libraryDependencies += servlet,
      libraryDependencies += httpComponents
    ) ++ zcheckJvmSettings
  ).dependsOn(
    rdf_jvm,
    ntriples_jvm
  )

  lazy val rdfTestSuite_common_js = Project(
    id = "rdf-test-suite_common_js",
    base = file("rdf-test-suite/.common_js"),
    settings = buildSettings ++ sjsDeps ++ linkedSources(rdfTestSuite_common_jvm) ++ Seq(
      resolvers += sonatypeRepo
     // libraryDependencies += scalajsJasmine
    ) ++ zcheckJsSettings
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js)

  /** `jena`, an RDF implementation for Apache Jena. */
  lazy val jena = Project(
    id = "jena",
    base = file("jena"),
    settings = buildSettings ++ Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += jenaLibs,
      libraryDependencies += logback,
      libraryDependencies += aalto
    )
  ) dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  /** `sesame`, an RDF implementation for Sesame. */
  lazy val sesame = Project(
    id = "sesame",
    base = file("sesame"),
    settings = buildSettings ++ Seq(
      libraryDependencies += sesameQueryAlgebra,
      libraryDependencies += sesameQueryParser,
      libraryDependencies += sesameQueryResult,
      libraryDependencies += sesameRioTurtle,
      libraryDependencies += sesameRioRdfxml,
      libraryDependencies += sesameSailMemory,
      libraryDependencies += sesameSailNativeRdf,
      libraryDependencies += sesameRepositorySail,
      libraryDependencies += jsonldJava
    )
  ) dependsOn(rdf_jvm, ntriples_jvm, rdfTestSuite_jvm % "test-internal->compile")

  /** `plantain`, a cross-compiled Scala implementation for RDF.
    *
    * It is composed of 3 source modules + two non-source modules.
    */
  lazy val plantain = Project(
    id = "plantain",
    base = file("plantain"),
    settings = buildSettings ++ Seq(
      publishMavenStyle := true
    )
  ).dependsOn(rdfTestSuite, plantain_jvm, plantain_common_jvm, plantain_js, plantain_common_js)
    .aggregate(plantain_jvm, plantain_js)

  lazy val plantain_jvm = Project(
    id = "plantain_jvm",
    base = file("plantain/jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += akkaHttpCore,
      libraryDependencies +=  sesameRioTurtle,
      libraryDependencies += jsonldJava,
      publishMavenStyle := true
    )
  ).dependsOn(rdf_jvm, ntriples_jvm, plantain_common_jvm % "compile;test->test", rdfTestSuite_jvm % "test-internal->compile")
    .aggregate(plantain_common_jvm)

  lazy val plantain_common_jvm = Project(
    id = "plantain_common_jvm",
    base = file("plantain/common"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      publishMavenStyle := true
    )
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test-internal->compile")

  lazy val plantain_js = Project(
    id = "plantain_js",
    base = file("plantain/js"),
    settings = buildSettings ++ Seq(
      publishMavenStyle := true
    )++ zcheckJsSettings
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js, ntriples_js, plantain_common_js % "compile;test->test", rdfTestSuite_js % "test-internal->compile")
    .aggregate(plantain_common_js)

  lazy val plantain_common_js = Project(
    id = "plantain_common_js",
    base = file("plantain/.common_js"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ linkedSources(plantain_common_jvm) ++ Seq(
      resolvers += sonatypeRepo,
      publishMavenStyle := true
    ) ++ zcheckJsSettings
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js, rdfTestSuite_js % "test-internal->compile")

  /** `rdfstorew`, a js only module binding rdfstore-js into banana-rdf
    * abstractions.
    */
  lazy val rdfstorew = Project(
    id = "rdfstorew",
    base = file("rdfstorew"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ Seq(
      resolvers += sonatypeRepo,
      jsDependencies += ProvidedJS / "rdf_store.js",
      jsDependencies += "org.webjars" % "momentjs" % "2.7.0" / "moment.js",
      skip in packageJSDependencies := false
    ) //++ jasmine_js
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdf_common_js, rdfTestSuite_js % "test-internal->compile")

  /** `examples`, a bunch of working examples using banana-rdf abstractions. */
  lazy val examples = Project(
    id = "examples",
    base = file("examples"),
    settings = buildSettings
  ) dependsOn(sesame, jena)

 */