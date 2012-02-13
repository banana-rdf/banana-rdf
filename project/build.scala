import sbt._
import Keys._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := "org.w3",
    version      := "0.1",
    scalaVersion := "2.9.1",
    parallelExecution in Test := false,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize", "-Ydependent-method-types")
  )

}

object YourProjectBuild extends Build {

  import BuildSettings._
  
  val junitInterface = "com.novocode" % "junit-interface" % "0.8"

  val scalacheck = "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9"
  
  val testsuiteDeps =
    Seq(libraryDependencies += junitInterface,
        libraryDependencies += scalacheck)
  
  val testDeps =
    Seq(libraryDependencies += junitInterface % "test")
  
  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "jena-arq" % "2.9.0-incubating")
      
  lazy val pimpMyRdf = Project(
    id = "pimp-my-rdf",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(
      algebraic,
      core,
      graphIsomorphism,
      transformer,
      transformerTestSuite,
      nTriplesParser,
      nTriplesParserTestSuite,
      jena))
  
  lazy val algebraic = Project(
    id = "algebraic",
    base = file("algebraic"),
    settings = buildSettings
  )
  
  lazy val core = Project(
    id = "core",
    base = file("core"),
    settings = buildSettings
  ) dependsOn (algebraic)

  lazy val graphIsomorphism = Project(
    id = "graph-isomorphism",
    base = file("graph-isomorphism"),
    settings = buildSettings
  ) dependsOn (core)

  lazy val transformer = Project(
    id = "transformer",
    base = file("transformer"),
    settings = buildSettings
  ) dependsOn (core)

  lazy val transformerTestSuite = Project(
    id = "transformer-testsuite",
    base = file("transformer-testsuite"),
    settings = buildSettings ++ testsuiteDeps
  ) dependsOn (core, transformer, graphIsomorphism)

  lazy val jena = Project(
    id = "jena",
    base = file("jena"),
    settings = buildSettings ++ jenaDeps ++ testDeps
  ) dependsOn (core, graphIsomorphism, transformer, nTriplesParser, nTriplesParserTestSuite % "test")

  lazy val nTriplesParser = Project(
    id = "n-triples-parser",
    base = file("n-triples-parser"),
    settings = buildSettings ++ jenaDeps
  ) dependsOn (core)
  
  lazy val nTriplesParserTestSuite = Project(
    id = "n-triples-parser-test-suite",
    base = file("n-triples-parser-test-suite"),
    settings = buildSettings ++ testsuiteDeps ++ jenaDeps
  ) dependsOn (nTriplesParser, graphIsomorphism)

}

