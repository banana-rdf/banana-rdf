import sbt._
import sbt.Keys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings

import bintray.Plugin._
import bintray.Keys._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++ defaultScalariformSettings ++ bintrayPublishSettings ++ Seq (
    organization := "org.w3",
    version      := "0.6-SNAPSHOT",
    scalaVersion := "2.10.4",
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
  
  val scalaActors = "org.scala-lang" % "scala-actors" % "2.10.2"

  val akka = "com.typesafe.akka" %% "akka-actor" % "2.2.0"
  val akkaTransactor = "com.typesafe.akka" %% "akka-transactor" % "2.2.0"

//  val scalaStm = "org.scala-tools" %% "scala-stm" % "0.7"

  val asyncHttpClient = "com.ning" % "async-http-client" % "1.7.12"

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.4"

  val jodaTime = "joda-time" % "joda-time" % "2.1"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val jodatimeDeps = Seq(
    libraryDependencies += jodaTime % "provided",
    libraryDependencies += jodaConvert % "provided")

  val scalatest = "org.scalatest" %% "scalatest" % "2.0"
  
  val testsuiteDeps =
    Seq(
//      libraryDependencies += scalaActors,
      libraryDependencies += scalatest
    )

  val iterateeDeps = "com.typesafe.play" %% "play-iteratees" % "2.2.1"
  val playDeps = "com.typesafe.play" %% "play" % "2.2.1"

  val reactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.9" excludeAll(ExclusionRule(organization = "io.netty"), ExclusionRule(organization = "play"))

  val testDeps =
    Seq(
//      libraryDependencies += scalaActors % "test",
      libraryDependencies += scalatest % "test"
    )
  
  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "apache-jena-libs" % "2.11.1" ,//excludeAll(ExclusionRule(organization = "org.slf4j")),
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided",
      libraryDependencies += "com.fasterxml" % "aalto-xml" % "0.9.7"
  )

  val sesameVersion = "2.7.6"
  
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

  val pub = TaskKey[Unit]("pub")

  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ Unidoc.settings ++ Seq(
      pub := (),
      pub <<= pub.dependsOn(publish in rdf, publish in jena, publish in sesame)),
    aggregate = Seq(
      rdf,
      rdfTestSuite,
      jena,
      sesame,
      examples))
  
  lazy val rdf = Project(
    id = "banana-rdf",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      publishMavenStyle := true
    )
  )

  lazy val patch = Project(
    id = "patch",
    base = file("patch"),
    settings = buildSettings ++ testDeps ++ Seq(
      publishMavenStyle := true
    )
  ) dependsOn (rdf, jena, rdfTestSuite % "test")

  lazy val rdfTestSuite = Project(
    id = "banana-rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ testsuiteDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ) dependsOn (rdf)

  lazy val jena = Project(
    id = "banana-jena",
    base = file("jena"),
    settings = buildSettings ++ jenaTestWIPFilter ++ jenaDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf, rdfTestSuite % "test")
  
  lazy val sesame = Project(
    id = "banana-sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameTestWIPFilter ++ sesameDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf, rdfTestSuite % "test")

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
    settings = buildSettings ++ testDeps ++ sesameCoreDeps ++ Seq(
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

