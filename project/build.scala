import bintray.Plugin._
import bintray.Keys._
import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings
import sbt.Keys._
import sbt.{ExclusionRule, _}

import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys._
import scala.scalajs.sbtplugin.ScalaJSPlugin._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++ defaultScalariformSettings ++ bintrayPublishSettings ++ Seq (
    organization := "org.w3",
    version      := "0.6-SNAPSHOT",
    scalaVersion := "2.11.2",
    crossScalaVersions := Seq("2.11.2", "2.10.4"),
    javacOptions ++= Seq("-source","1.7", "-target","1.7"),
    fork := false,
    parallelExecution in Test := false,
    offline := true,
    // TODO
    testOptions in Test += Tests.Argument("-oD"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140", "-Yinline-warnings"),
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    publishArtifact in Test := false,
//    publishTo := {
//      val nexus = "https://oss.sonatype.org/"
//      if (version.value.trim.endsWith("SNAPSHOT"))
//        Some("snapshots" at nexus + "content/repositories/snapshots")
//      else
//        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
//    },
    description := "RDF framework for Scala",
    startYear := Some(2012),
    pomIncludeRepository := { _ => false },
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
    // bintray
    repository in bintray := "banana-rdf",
    bintrayOrganization in bintray := None,
    licenses += ("W3C", url("http://opensource.org/licenses/W3C"))
  )

  val jenaTestWIPFilter = Seq (
    testOptions in Test += Tests.Argument("-l", "org.w3.banana.jenaWIP")
  )

  val sesameTestWIPFilter = Seq (
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
      libraryDependencies += scalatest
    )

  val iterateeDeps = "com.typesafe.play" %% "play-iteratees" % "2.3.0"
  val playDeps = "com.typesafe.play" %% "play" % "2.3.0"

  val reactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.akka23-SNAPSHOT" excludeAll(ExclusionRule(organization = "io.netty"), ExclusionRule(organization = "play"))
  val reactiveMongoDeps = Seq(
        resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/" )

  val testDeps =
    Seq(
//      libraryDependencies += scalaActors % "test",
      libraryDependencies += scalatest % "test"
    )
  
  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "2.11.2" ,//excludeAll(ExclusionRule(organization = "org.slf4j")),
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

  val scalaJsDeps = scalaJSSettings ++ 
    Seq(
      resolvers += "bblfish.net" at "http://bblfish.net/work/repo/releases/"
    )

  val scalaz_js = "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.0.6"
  
  val pub = TaskKey[Unit]("pub")

  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ Unidoc.settings ++ Seq(
      pub := (),
      pub <<= pub.dependsOn(publish in rdf_jvm, publish in jena, publish in sesame)),
    aggregate = Seq(
      rdf_jvm,
      rdf_common_jvm,
      rdf_js,
      rdf_common_js,
      rdfTestSuite,
      jena,
      sesame,
      plantain,
      pome,
      rdfstorew,
      examples))
  
  lazy val rdf_jvm = Project(
    id = "banana-rdf",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      target := target.value / "jvm",
      publishMavenStyle := true
    )
  ).dependsOn(rdf_common_jvm  % "compile->compile;test->test")

  lazy val rdf_common_jvm = Project(
    id = "banana-rdf_common_jvm",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      //libraryDependencies += akka,
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      target := target.value / "common_jvm",
      publishMavenStyle := true
    )
  )

  lazy val rdf_js = Project(
    id = "banana-rdf_js",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ scalaJsDeps ++ Seq(
      target := target.value / "js",
      publishMavenStyle := true
    )
  ).dependsOn(rdf_common_js % "compile->compile;test->test")

  lazy val rdf_common_js = Project(
    id = "banana-rdf_common_js",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ scalaJsDeps ++ Seq(
      libraryDependencies += scalaz_js,
      libraryDependencies += jodaTime,    //Will not work --- pure java
      libraryDependencies += jodaConvert, //Will not work --- pure java
      //target :=  "rdf/target",
      target := target.value / "common_js",
      publishMavenStyle := true
    )
  )

  lazy val patch = Project(
    id = "ldpatch",
    base = file("ldpatch"),
    settings = buildSettings ++ testDeps ++ Seq(
      publishMavenStyle := true,
      libraryDependencies += "org.parboiled" %% "parboiled" % "2.0.0"
    )
  ) dependsOn (rdf_jvm, jena, rdfTestSuite % "test")

  lazy val rdfTestSuite = Project(
    id = "banana-rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ testsuiteDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ) dependsOn (rdf_jvm)

  lazy val rdfTestSuiteJS = Project(
    id = "banana-scalajs-rdf-test-suite",
    base = file("rdf-test-suite/rdf-test-suite_js"),
    settings = buildSettings ++  scalaJSSettings ++ Seq(
      libraryDependencies += scalatest,
      libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion
    )
  ) dependsOn (rdf_js)

  lazy val jena = Project(
    id = "banana-jena",
    base = file("jena"),
    settings = buildSettings ++ jenaTestWIPFilter ++ jenaDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf_jvm, rdfTestSuite % "test")
  
  lazy val sesame = Project(
    id = "banana-sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameTestWIPFilter ++ sesameDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf_jvm, rdfTestSuite % "test")

  lazy val plantain = Project(
    id = "banana-plantain",
    base = file("plantain"),
    settings = buildSettings ++ testDeps ++  Seq(
      //      libraryDependencies += "org.semarglproject" % "semargl-rdf" % "0.6.1",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % sesameVersion,
      libraryDependencies += "com.typesafe.akka" %% "akka-http-core-experimental" % "0.4"
    )
  ) dependsOn (rdf_jvm, rdfTestSuite % "test")

  lazy val pome = Project(
    id = "banana-pome",
    base = file("pome"),
    settings =   buildSettings ++ testDeps ++ scalaJSSettings ++ Seq(
      resolvers += "bblfish.net" at "http://bblfish.net/work/repo/releases/",
      libraryDependencies += "com.github.japgolly.fork.scalaz" %%% "scalaz-core" % "7.1.0"
    )
  ) dependsOn (rdf_js, rdfTestSuite % "test", rdfTestSuiteJS % "test")

  lazy val rdfstorew = Project(
    id = "banana-rdfstorew",
    base = file("rdfstorew"),
    //settings =  buildSettings ++  testDeps ++ scalaJSSettings ++ Seq(
    settings =  buildSettings ++ scalaJsDeps ++ Seq(
    //settings =  scalaJSSettings ++ buildSettings ++ testDeps ++ Seq(
      jsDependencies += ProvidedJS / "rdf_store.js",
      jsDependencies += "org.webjars" % "momentjs" % "2.7.0" / "moment.js",
      //resolvers += "bblfish.net" at "http://bblfish.net/work/repo/releases/",
      libraryDependencies += scalaz_js,
      skip in packageJSDependencies := false
    )
  ) dependsOn (rdf_js, rdfTestSuiteJS % "test")

  lazy val examples = Project(
    id = "examples",
    base = file("examples"),
    settings = buildSettings
  ) dependsOn (sesame, jena)

  // this is _experimental_
  // please do not add this projet to the main one
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
  ) dependsOn (rdfTestSuite % "test")

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
  ) dependsOn (rdfTestSuite % "test")

  
}

