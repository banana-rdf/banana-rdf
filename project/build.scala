import sbt._
import Keys._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := "org.w3",
    version      := "0.1",
    scalaVersion := "2.10.0-M1",
    parallelExecution in Test := false,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-Yvirtpatmat", "-optimize")
  )

}

object YourProjectBuild extends Build {

  import BuildSettings._
  
  val junitInterface = "com.novocode" % "junit-interface" % "0.8"
  
  val testsuiteDeps =
    Seq(libraryDependencies += junitInterface)
  
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
      rdfModel,
      graphIsomorphism,
      transformer,
      jena))
  
  lazy val algebraic = Project(
    id = "algebraic",
    base = file("algebraic"),
    settings = buildSettings
  )
  
  lazy val rdfModel = Project(
    id = "rdf-model",
    base = file("core"),
    settings = buildSettings
  ) dependsOn (algebraic)

  lazy val graphIsomorphism = Project(
    id = "graph-isomorphism",
    base = file("graph-isomorphism"),
    settings = buildSettings
  ) dependsOn (rdfModel)

  lazy val transformer = Project(
    id = "transformer",
    base = file("transformer"),
    settings = buildSettings
  ) dependsOn (rdfModel)

  lazy val jena = Project(
    id = "jena",
    base = file("jena"),
    settings = buildSettings ++ jenaDeps ++ testDeps
  ) dependsOn (rdfModel, graphIsomorphism, transformer, nTriplesParser, nTriplesParserTestSuite % "test")

  lazy val nTriplesParser = Project(
    id = "n-triples-parser",
    base = file("n-triples-parser"),
    settings = buildSettings ++ jenaDeps
  ) dependsOn (rdfModel)
  
  lazy val nTriplesParserTestSuite = Project(
    id = "n-triples-parser-test-suite",
    base = file("n-triples-parser-test-suite"),
    settings = buildSettings ++ testsuiteDeps
  ) dependsOn (nTriplesParser, graphIsomorphism)

}

