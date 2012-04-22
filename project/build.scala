import sbt._
import sbt.Keys._
import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := "org.w3",
    version      := "0.1-SNAPSHOT",
    scalaVersion := "2.9.1",

    parallelExecution in Test := false,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize"),
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    ensimeConfig := sexp(
      key(":compiler-args"), sexp("-Ywarn-dead-code", "-Ywarn-shadowing"),
      key(":formatting-prefs"), sexp(
        key(":rewriteArrowSymbols"), true,
	key(":doubleIndentClassDeclaration"), true
      )
    ),
    publishTo := Some(Resolver.ssh("bblfish repository", "bblfish.net", "/home/hjs/htdocs/work/repo/snapshots") as
      ("hjs",new File("/Users/hjs/.ssh/id_dsa")))

  )

}

object YourProjectBuild extends Build {

  import BuildSettings._
  
  import com.typesafe.sbteclipse.plugin.EclipsePlugin._
  
  val akka = "com.typesafe.akka" % "akka-actor" % "2.0-RC4"

  val asyncHttpClient = "com.ning" % "async-http-client" % "1.8.0-SNAPSHOT"

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0-SNAPSHOT"

  val junitInterface = "com.novocode" % "junit-interface" % "0.8"
  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9"
  val scalatest = "org.scalatest" %% "scalatest" % "1.7.1"
  
  val testsuiteDeps =
    Seq(libraryDependencies += junitInterface,
        libraryDependencies += scalatest,
        libraryDependencies += scalacheck)
  
  val testDeps =
    Seq(libraryDependencies += junitInterface % "test",
        libraryDependencies += scalatest % "test")
  
  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "jena-arq" % "2.9.0-incubating",
      libraryDependencies += "com.fasterxml" % "aalto-xml" % "0.9.7"
  )
  
  val sesameDeps =
    Seq(
      resolvers += "sesame-repo-releases" at "http://repo.aduna-software.org/maven2/releases/",
      libraryDependencies += "org.openrdf.sesame" % "sesame-runtime" % "2.6.4")

  val n3Deps =
    Seq( libraryDependencies += "org.apache.abdera" % "abdera-i18n" % "1.1.2" )
  
  lazy val pimpMyRdf = Project(
    id = "pimp-my-rdf",
    base = file("."),
    settings = buildSettings ++ Seq(EclipseKeys.skipParents in ThisBuild := false),
    aggregate = Seq(
      rdf,
      rdfTestSuite,
      simpleRdf,
      n3,
      n3TestSuite,
      jena,
      sesame,
      linkedData,
      diesel))
  
  lazy val rdf = Project(
    id = "rdf",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += scalaz)
  )

  lazy val rdfTestSuite = Project(
    id = "rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ testsuiteDeps
  ) dependsOn (rdf)

  val simpleRdf = Project(
    id = "simple-rdf",
    base = file("simple-rdf"),
    settings = buildSettings ++ testDeps
  ) dependsOn (rdf, n3, rdfTestSuite % "test", util % "test")
  
  lazy val jena = Project(
    id = "jena",
    base = file("jena"),
    settings = buildSettings ++ jenaDeps ++ testDeps
  ) dependsOn (rdf, n3, rdfTestSuite % "test")
  
  lazy val sesame = Project(
    id = "sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameDeps ++ testDeps
  ) dependsOn (rdf, n3, rdfTestSuite % "test")
  
  lazy val util = Project(
    id = "util",
    base = file("util"),
    settings = buildSettings ++ jenaDeps
  ) dependsOn (rdf, jena)
  
  lazy val n3 = Project(
    id = "n3",
    base = file("n3"),
    settings = buildSettings ++ jenaDeps ++ n3Deps
  ) dependsOn (rdf)
  
  lazy val n3TestSuite = Project(
    id = "n3-test-suite",
    base = file("n3-test-suite"),
    settings = buildSettings ++ testsuiteDeps
  ) dependsOn (n3, jena % "test", sesame % "test", simpleRdf % "test", util % "test")

  lazy val linkedData = Project(
    id = "linked-data",
    base = file("linked-data"),
    settings = buildSettings ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += asyncHttpClient)
  ) dependsOn (rdf, sesame, jena)

  lazy val diesel = Project(
    id = "diesel",
    base = file("diesel"),
    settings = buildSettings ++ testDeps
  ) dependsOn (rdf, jena % "test", sesame % "test")

  // lazy val peeler = Project(
  //   id = "peeler",
  //   base = file("peeler"),
  //   settings = buildSettings
  // ) dependsOn (rdf)
  
  // lazy val dslTestSuite = Project(
  //   id = "peeler",
  //   base = file("peeler"),
  //   settings = buildSettings
  // ) dependsOn (diesel, peeler, jena % "test", sesame % "test")
  

}

