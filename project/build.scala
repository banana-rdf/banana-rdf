import bintray.Plugin._
import bintray.Keys._
import com.inthenow.sbt.scalajs.SbtScalajs
import com.inthenow.sbt.scalajs.SbtScalajs._
import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings
import sbt.Keys._
import sbt.{ExclusionRule, _}

import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = publicationSettings ++ defaultScalariformSettings ++ Seq(
    organization := "org.w3",
    version := "0.7-SNAPSHOT",
    scalaVersion := "2.11.2",
    crossScalaVersions := Seq("2.11.2", "2.10.4"),
    javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
    fork := false,
    parallelExecution in Test := false,
    offline := true,
    // TODO
    testOptions in Test += Tests.Argument("-oDS"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140", "-Yinline-warnings"),
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    description := "RDF framework for Scala",
    startYear := Some(2012),
    pomIncludeRepository := { _ => false},
    pomExtra := (
      <url>https://github.com/w3c/banana-rdf</url>
        <developers>
          <developer>
            <id>betehess</id>
            <name>Alexandre Bertails</name>
            <url>http://bertails.org/</url>
          </developer>
          <developer>
            <id>antoniogarrote</id>
            <name>Antonio Garrote</name>
            <url>https://github.com/antoniogarrote/</url>
          </developer>
          <developer>
            <id>InTheNow</id>
            <name>Alistair Johnson</name>
            <url>https://github.com/inthenow</url>
          </developer>
          <developer>
            <id>bblfish</id>
            <name>Henry Story</name>
            <url>http://bblfish.net/</url>
          </developer>
        </developers>
        <scm>
          <url>git@github.com:w3c/banana-rdf.git</url>
          <connection>scm:git:git@github.com:w3c/banana-rdf.git</connection>
        </scm>
      ),
    licenses +=("W3C", url("http://opensource.org/licenses/W3C"))
  )

  //sbt -Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/
  //sbt -Dbanana.publish=bintray
  def publicationSettings =
    (Option(System.getProperty("banana.publish")) match {
      case Some("bintray") => Seq(
        // bintray
        repository in bintray := "banana-rdf",
        bintrayOrganization in bintray := None
      ) ++ bintrayPublishSettings
      case opt: Option[String] => {
        Seq(
          publishTo <<= version { (v: String) =>
            val nexus = "https://oss.sonatype.org/"
            val other = opt.map(_.split(":"))
            if (v.trim.endsWith("SNAPSHOT")) {
              val repo = other.map(p => Resolver.ssh("banana.publish specified server", p(0), p(1) + "snapshots"))
              repo.orElse(Some("snapshots" at nexus + "content/repositories/snapshots"))
            } else {
              val repo = other.map(p => Resolver.ssh("banana.publish specified server", p(0), p(1) + "resolver"))
              repo.orElse(Some("releases" at nexus + "service/local/staging/deploy/maven2"))
            }
          }
        )
      }
    }) ++ Seq(publishArtifact in Test := false)

}

object BananaRdfBuild extends Build {

  import BuildSettings._
  import Dependencies._

  val johnsonRepo = "JohnsonUSM repository" at "http://johnsonusm.com:8020/nexus/content/repositories/releases/"

  val sjsDeps = Seq(
    resolvers += Resolver.url("scala-js-releases", url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(Resolver.ivyStylePatterns)
  ) ++ scalajsJsSettings

  /** `banana`, the root project. */
  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ Unidoc.settings
  ).dependsOn(banana_js, banana_jvm)
   .aggregate(banana_js, banana_jvm)

  /** `banana_js`, a js only meta project. */
  lazy val banana_js = Project(
    id = "banana_js",
    base = file(".banana_js"),
    settings = buildSettings ++ Unidoc.settings ++ Seq(
      aggregate in Test in rdf_js := false,
      aggregate in Test in rdfTestSuite_js := false
    )
  ).dependsOn(rdf_js, rdfTestSuite_js, plantain_js)
   .aggregate(rdf_js, rdfTestSuite_js, plantain_js)

  /** `banana_jvm`, a jvm only meta project. */
  lazy val banana_jvm = Project(
    id = "banana_jvm",
    base = file(".banana_jvm"),
    settings = buildSettings ++ Unidoc.settings ++ Seq (
      aggregate in Test in rdf_jvm := false,
      aggregate in Test in rdfTestSuite_jvm := false
    )
  ).dependsOn(rdf_jvm, rdfTestSuite_jvm, jena, sesame, plantain_jvm, examples)
   .aggregate(rdf_jvm, rdfTestSuite_jvm, jena, sesame, plantain_jvm, examples)

  /** A virtual module for gathering experimental ones. */
  lazy val experimental = Project(
    id = "experimental",
    base = file(".experimental"),
    settings = buildSettings ++ Unidoc.settings,
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
    base = file("rdf/rdf_jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).dependsOn(rdf_common_jvm).aggregate(rdf_common_jvm)

  lazy val rdf_common_jvm = Project(
    id = "rdf_common_jvm",
    base = file("rdf/rdf_common_jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      publishMavenStyle := true
    )
  )

  lazy val rdf_js = Project(
    id = "rdf_js",
    base = file("rdf/rdf_js"),
    settings = buildSettings ++ sjsDeps ++ Seq(
      aggregate in Test := false,
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_common_js).aggregate(rdf_common_js)

  lazy val rdf_common_js = Project(
    id = "rdf_common_js",
    base = file("rdf/.rdf_common_js"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ linkedSources(rdf_common_jvm) ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)


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
        case "2.11.2" => Seq("org.scala-lang" % "scala-reflect" % "2.11.2")
        case _ => Seq.empty
      },
      libraryDependencies += scalatest % "test"
    )
  ) dependsOn(rdf_jvm, jena, rdfTestSuite_jvm % "test")

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
  ).dependsOn(rdfTestSuite_jvm, rdfTestSuite_common_jvm, rdfTestSuite_js, rdfTestSuite_common_js)
    .aggregate(rdfTestSuite_jvm, rdfTestSuite_js)

  lazy val rdfTestSuite_jvm = Project(
    id = "rdf-test-suite_jvm",
    base = file("rdf-test-suite/rdf-test-suite_jvm"),
    settings = buildSettings ++ Seq(
      aggregate in Test := false
    )
  ).dependsOn(rdf_jvm, rdfTestSuite_common_jvm).aggregate(rdfTestSuite_common_jvm)

  lazy val rdfTestSuite_js = Project(
    id = "rdf-test-suite_js",
    base = file("rdf-test-suite/rdf-test-suite_js"),
    settings = buildSettings ++ Seq(
      aggregate in Test := false
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdfTestSuite_common_js)
    .aggregate(rdfTestSuite_common_js)

  lazy val rdfTestSuite_common_jvm = Project(
    id = "rdf-test-suite_common_jvm",
    base = file("rdf-test-suite/rdf-test-suite_common_jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      resolvers += johnsonRepo,
      libraryDependencies += scalatest,
      libraryDependencies += jasmine_jvm,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ).dependsOn(rdf_jvm)

  lazy val rdfTestSuite_common_js = Project(
    id = "rdf-test-suite_common_js",
    base = file("rdf-test-suite/.rdf-test-suite_common_js"),
    settings = buildSettings ++ sjsDeps ++ linkedSources(rdfTestSuite_common_jvm) ++ Seq(
      resolvers += johnsonRepo,
      libraryDependencies += scalajsJasmine
    ) ++ jasmine_js
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
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test")

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
      libraryDependencies += sesameRepositorySail
    )
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test")

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
    base = file("plantain/plantain_jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      publishMavenStyle := true
    )
  ).dependsOn(rdf_jvm, plantain_common_jvm % "compile;test->test", rdfTestSuite_jvm % "test")
    .aggregate(plantain_common_jvm)

  lazy val plantain_common_jvm = Project(
    id = "plantain_common_jvm",
    base = file("plantain/plantain_common_jvm"),
    settings = buildSettings ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += sesameRioTurtle,
      libraryDependencies += akkaHttpCore,
      publishMavenStyle := true
    )
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test")

  lazy val plantain_js = Project(
    id = "plantain_js",
    base = file("plantain/plantain_js"),
    settings = buildSettings ++ testAutoOpt ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js, plantain_common_js % "compile;test->test", rdfTestSuite_js % "test")
    .aggregate(plantain_common_js)

  lazy val plantain_common_js = Project(
    id = "plantain_common_js",
    base = file("plantain/.plantain_common_js"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ testAutoOpt ++ linkedSources(plantain_common_jvm) ++ Seq(
      resolvers += johnsonRepo,
      publishMavenStyle := true
    ) ++ jasmine_jsTest
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js, rdfTestSuite_js % "test")

  /** `rdfstorew`, a js only module binding rdfstore-js into banana-rdf
    * abstractions.
    */
  lazy val rdfstorew = Project(
    id = "rdfstorew",
    base = file("rdfstorew"),
    settings = buildSettings ++ sjsDeps ++ scalaz_js ++ Seq(
      resolvers += johnsonRepo,
      jsDependencies += ProvidedJS / "rdf_store.js",
      jsDependencies += "org.webjars" % "momentjs" % "2.7.0" / "moment.js",
      skip in packageJSDependencies := false
    ) ++ jasmine_js
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdf_common_js, rdfTestSuite_js % "test->test")

  /** `examples`, a bunch of working examples using banana-rdf abstractions. */
  lazy val examples = Project(
    id = "examples",
    base = file("examples"),
    settings = buildSettings
  ) dependsOn(sesame, jena)

}

