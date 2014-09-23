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
            <url>http://bertails.org</url>
          </developer>
          <developer>
            <id>bblfish</id>
            <name>Henry Story</name>
            <url>http://bblfish.net/</url>
          </developer>
          <developer>
            <id>antoniogarrote</id>
            <name>Antonio Garrote</name>
            <url>https://github.com/antoniogarrote/</url>
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

  val jenaTestWIPFilter = Seq(
    testOptions in Test += Tests.Argument("-l", "org.w3.banana.jenaWIP")
  )

  val sesameTestWIPFilter = Seq(
    testOptions in Test += Tests.Argument("-l", "org.w3.banana.sesameWIP")
  )

}

object BananaRdfBuild extends Build {

  import BuildSettings._

  // rdfstorew settings
  skip in ScalaJSKeys.packageJSDependencies := false


  val scalaActors = "org.scala-lang" % "scala-actors" % "2.10.2"

  val akka = "com.typesafe.akka" %% "akka-actor" % "2.3.4"
  val akkaTransactor = "com.typesafe.akka" %% "akka-transactor" % "2.3.4"


  //  val scalaStm = "org.scala-tools" %% "scala-stm" % "0.7"

  val asyncHttpClient = "com.ning" % "async-http-client" % "1.7.12"

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.6"
  val jodaTime = "joda-time" % "joda-time" % "2.1"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val jodatimeDeps = Seq(
    libraryDependencies += jodaTime % "provided",
    libraryDependencies += jodaConvert % "provided")

  val scalatest = "org.scalatest" %% "scalatest" % "2.2.0"

  val testsuiteDeps =
    Seq(
      //      libraryDependencies += scalaActors,
      libraryDependencies += scalatest,
      resolvers += "JohnsonUSM repository" at "http://johnsonusm.com:8020/nexus/content/repositories/releases/",
      libraryDependencies += "com.github.inthenow" %% "jasmine_jvm" % "0.2.0" % "test"
    )

  val iterateeDeps = "com.typesafe.play" %% "play-iteratees" % "2.3.0"
  val playDeps = "com.typesafe.play" %% "play" % "2.3.0"

  val reactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT" excludeAll(ExclusionRule(organization = "io.netty"), ExclusionRule(organization = "play"))
  val reactiveMongoDeps = Seq(
    resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

  val testDeps =
    Seq(
//      libraryDependencies += scalaActors % "test",
      resolvers += "JohnsonUSM repository" at "http://johnsonusm.com:8020/nexus/content/repositories/releases/",
      libraryDependencies += scalatest % "test",
      libraryDependencies += "com.github.inthenow" %% "jasmine_jvm" % "0.2.0" //% "test"
    )

  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "2.11.2", //excludeAll(ExclusionRule(organization = "org.slf4j")),
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided",
      libraryDependencies += "com.fasterxml" % "aalto-xml" % "0.9.7"
    )

  val sesameVersion = "2.8.0-beta1"

  val sesameCoreDeps =
    Seq(
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryparser-sparql" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-rdfxml" % sesameVersion)

  val sesameDeps = sesameCoreDeps ++
    Seq(
      libraryDependencies += "org.openrdf.sesame" % "sesame-sail-memory" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-sail-nativerdf" % sesameVersion,
      libraryDependencies += "org.openrdf.sesame" % "sesame-repository-sail" % sesameVersion)

  val sjsTestDeps =
    Seq(
      //      libraryDependencies += scalaActors % "test",
      resolvers += "JohnsonUSM repository" at "http://johnsonusm.com:8020/nexus/content/repositories/releases/",
      libraryDependencies += "com.github.inthenow" %%% "jasmine_js" % "0.2.0" //% "test"
    )


  val sjsDeps = //scalaJSSettings ++
    Seq(
      resolvers += Resolver.url("scala-js-releases", url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(Resolver.ivyStylePatterns),
      resolvers += "bblfish.net" at "http://bblfish.net/work/repo/releases/"
    ) ++ scalajsJsSettings

  val scalaz_js = Seq(libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.0.6")

  val pub = TaskKey[Unit]("pub")

  //todo: add a way so that it is easy to get the whole to compile
  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ Unidoc.settings ++ Seq(
      pub :=(),
      pub <<= pub.dependsOn(publish in rdf_jvm, publish in jena, publish in sesame)),
    aggregate = Seq(
      rdf_jvm,
      rdf_common_jvm,
      rdf_js,
      rdf_common_js,
      rdfTestSuite_jvm,
      //rdfTestSuiteJS,
      jena,
      sesame,
      plantain,
      //pome,
      //rdfstorew,
      examples))

  lazy val rdf_jvm = Project(
    id = "banana-rdf",
    base = file("rdf/rdf_jvm"),
    settings = buildSettings ++ testDeps ++ scalajsJvmSettings ++
      Seq(
        //target := target.value / "jvm",
        publishMavenStyle := true
      )
  ).dependsOn(rdf_common_jvm % "compile->compile;test->test")

  lazy val rdf_common_jvm = Project(
    id = "banana-rdf_common_jvm",
    base = file("rdf/rdf_common_jvm"),
    settings = buildSettings ++ testDeps ++ scalajsJvmSettings ++ Seq(
      //libraryDependencies += akka,
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      publishMavenStyle := true
    )
  )

  lazy val rdf_js = Project(
    id = "banana-rdf_js",
    base = file("rdf/rdf_js"),
    settings = buildSettings ++ sjsTestDeps ++ sjsDeps ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_common_js % "compile->compile;test->test")

  lazy val rdf_common_js = Project(
    id = "banana-rdf_common_js",
    base = file("rdf/.rdf_common_js"),
    settings = buildSettings ++ sjsDeps ++ sjsTestDeps ++ scalaz_js ++ linkedSources(rdf_common_jvm) ++ Seq(
       publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs)

  lazy val ldpatch = Project(
    id = "ldpatch",
    base = file("ldpatch"),
    settings = buildSettings ++ testDeps ++ Seq(
      publishMavenStyle := true,
      libraryDependencies += "org.parboiled" %% "parboiled" % "2.0.0",
      // this will be needed until parboiled 2.0.1 gets released
      // see https://github.com/sirthias/parboiled2/issues/84#
      libraryDependencies <++= scalaVersion {
        case "2.11.2" => Seq("org.scala-lang" % "scala-reflect" % "2.11.2")
        case _ => Seq.empty
      }
    )
  ) dependsOn(rdf_jvm, jena, rdfTestSuite_jvm % "test")

  /**
   *  banana-rdf-test-suite, a x-compiled test suite module composed of 3 source modules + two non-source modules
   */
  lazy val rdfTestSuite_jvm = Project(
    id = "banana-rdf-test-suite_jvm",
    base = file("rdf-test-suite/rdf-test-suite_jvm"), 
    settings = buildSettings ++ testsuiteDeps ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ) dependsOn (rdf_jvm % "compile;test->test", rdfTestSuite_common_jvm % "compile;test->test")

  lazy val rdfTestSuite_js = Project(
    id = "banana-rdf-test-suite_js",
    base = file("rdf-test-suite/rdf-test-suite_js"),
    settings = buildSettings ++ sjsDeps ++ sjsTestDeps ++ Seq(
      libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js % "compile;test->test", rdf_common_js % "compile;test->test", rdfTestSuite_common_js % "compile;test->test")

  lazy val rdfTestSuite_common_jvm = Project(
    id = "banana-rdf-test-suite_common_jvm",
    base = file("rdf-test-suite/rdf-test-suite_common_jvm"),
    settings = buildSettings ++ testsuiteDeps ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ).dependsOn(rdf_jvm % "compile;test->test")

  lazy val rdfTestSuite_common_js = Project(
    id = "banana-rdf-test-suite_common_js",
    base = file("rdf-test-suite/.rdf-test-suite_common_js"),
    settings = buildSettings ++ sjsDeps ++ sjsTestDeps ++ linkedSources(rdfTestSuite_common_jvm) ++ Seq(
      libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js % "compile;test->test", rdf_common_js % "compile;test->test")

  /**
   * banana-jena
   */
  lazy val jena = Project(
    id = "banana-jena",
    base = file("jena"),
    settings = buildSettings ++ jenaTestWIPFilter ++ jenaDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test")

  /**
   * banana-sesame
   */
  lazy val sesame = Project(
    id = "banana-sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameTestWIPFilter ++ sesameDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn(rdf_jvm, rdfTestSuite_jvm % "test")

  /**
   * banana-plantain, a x-compiled module composed of 3 source modules + two non-source modules
   */
  lazy val plantain = Project(
    id = "banana-plantain",
    base = file("plantain"),
    settings = buildSettings ++ Seq(
      publishMavenStyle := true
    )
  ).dependsOn(plantain_jvm, plantain_common_jvm, plantain_js, plantain_common_js)
    .aggregate(plantain_jvm, plantain_common_jvm, plantain_js, plantain_common_js)

  lazy val plantain_jvm = Project(
    id = "banana-plantain_jvm",
    base = file("plantain/plantain_jvm"),
    settings = buildSettings ++ testDeps ++ scalajsJvmSettings ++ Seq(
      //      libraryDependencies += "org.semarglproject" % "semargl-rdf" % "0.6.1",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion,
      libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % "0.4",
      publishMavenStyle := true
    )
  ) dependsOn(rdf_jvm  % "compile;test->test", plantain_common_jvm % "compile;test->test", rdfTestSuite_jvm % "test->compile")

  lazy val plantain_common_jvm = Project(
    id = "banana-plantain_common_jvm",
    base = file("plantain/plantain_common_jvm"),
    settings = buildSettings ++ testDeps ++ scalajsJvmSettings ++ Seq(
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion,
      libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % "0.4",
      publishMavenStyle := true
    )
  ) dependsOn(rdf_jvm % "compile;test->test", rdfTestSuite_jvm % "compile;test->compile")

  lazy val plantain_js = Project(
    id = "banana-plantain_js",
    base = file("plantain/plantain_js"),
    settings = buildSettings ++ scalaz_js ++ sjsDeps ++ sjsTestDeps ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js % "compile;test->test", plantain_common_js % "compile;test->test", rdfTestSuite_js % "test->compile", rdfTestSuite_jvm  % "test->compile")

  lazy val plantain_common_js = Project(
    id = "banana-plantain_common_js",
    base = file("plantain/.plantain_common_js"),
    settings = buildSettings ++ sjsDeps ++ sjsTestDeps ++ scalaz_js ++ linkedSources(plantain_common_jvm) ++ Seq(
      publishMavenStyle := true
    )
  ).enablePlugins(SbtScalajs).dependsOn(rdf_js % "compile;test->test", rdfTestSuite_js % "compile;test->compile")

  /**
   * banana-rdfstorew, a js only module
   */
  lazy val rdfstorew = Project(
    id = "banana-rdfstorew",
    base = file("rdfstorew"),
    settings = buildSettings ++ sjsDeps ++ sjsTestDeps ++ scalaz_js ++ Seq(
      jsDependencies += ProvidedJS / "rdf_store.js",
      jsDependencies += "org.webjars" % "momentjs" % "2.7.0" / "moment.js",
      //resolvers += "bblfish.net" at "http://bblfish.net/work/repo/releases/",
      skip in packageJSDependencies := false
    )
  ).enablePlugins(SbtScalajs)
    .dependsOn(rdf_js, rdf_common_js, rdfTestSuite_js% "test->test")

  lazy val examples = Project(
    id = "examples",
    base = file("examples"),
    settings = buildSettings
  ) dependsOn(sesame, jena)

  // this is _experimental_
  // please do not add this project to the main one
  lazy val experimental = Project(
    id = "experimental",
    base = file("experimental"),
    settings = buildSettings ++ testDeps ++ reactiveMongoDeps ++ sesameCoreDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += akkaTransactor,
      libraryDependencies += iterateeDeps,
      libraryDependencies += reactiveMongo,
      libraryDependencies += playDeps,
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
    )
  ) dependsOn (rdfTestSuite_jvm % "test")

  lazy val ldp = Project(
    id = "ldp",
    base = file("ldp"),
    settings = buildSettings ++ testDeps ++ sesameCoreDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += asyncHttpClient,
      libraryDependencies += akkaTransactor,
      libraryDependencies += scalaz,
      libraryDependencies += iterateeDeps,
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
    )
  ) dependsOn (rdfTestSuite_jvm % "test")


}

