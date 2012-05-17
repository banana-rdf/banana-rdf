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
    licenses := Seq("W3C License" -> url("http://opensource.org/licenses/W3C")),
    homepage := Some(url("https://github.com/w3c/banana-rdf"))
  )

}

object YourProjectBuild extends Build {

  import BuildSettings._
  
  import com.typesafe.sbteclipse.plugin.EclipsePlugin._
  
  val akka = "com.typesafe.akka" % "akka-actor" % "2.0.1"

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
  
  /* http://repo.aduna-software.org/maven2/releases/org/openrdf/sesame/ */
  val sesameDeps =
    Seq(
      resolvers += "sesame-repo-releases" at "http://repo.aduna-software.org/maven2/releases/",
      libraryDependencies += "org.openrdf.sesame" % "sesame-sail-memory" % "2.6.5",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % "2.6.5",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.6.5",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryparser-sparql" % "2.6.5",
      libraryDependencies += "org.openrdf.sesame" % "sesame-repository-sail" % "2.6.5")

  val n3Deps =
    Seq( libraryDependencies += "org.apache.abdera" % "abdera-i18n" % "1.1.2" )
  
  lazy val bananaRdf = Project(
    id = "banana-rdf",
    base = file("."),
    settings = buildSettings ++ Seq(EclipseKeys.skipParents in ThisBuild := false),
    aggregate = Seq(
      rdf,
      rdfTestSuite,
      simpleRdf,
      n3,
//      n3TestSuite,
      jena,
      sesame,
      linkedData))
  
  lazy val rdf = Project(
    id = "rdf",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += akka,
      libraryDependencies += scalaz,
      publishMavenStyle := true,
      publishTo <<= version { (v: String) =>
        val nexus = "https://oss.sonatype.org/"
        if (v.trim.endsWith("SNAPSHOT")) 
          Some("snapshots" at nexus + "content/repositories/snapshots") 
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <scm>
          <url>git@github.com:w3c/banana-rdf.git</url>
          <connection>scm:git:git@github.com:w3c/banana-rdf.git</connection>
        </scm>
        <developers>
          <developer>
            <id>betehess</id>
            <name>Alexandre Bertails</name>
            <url>http://bertails.org</url>
          </developer>
        </developers>)
    )
  )

  lazy val rdfTestSuite = Project(
    id = "rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ testsuiteDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf)

  val simpleRdf = Project(
    id = "simple-rdf",
    base = file("simple-rdf"),
    settings = buildSettings ++ testDeps
  ) dependsOn (rdf, n3, rdfTestSuite % "test", jena % "test")
  
  lazy val jena = Project(
    id = "jena",
    base = file("jena"),
    settings = buildSettings ++ jenaDeps ++ testDeps ++ Seq(
      libraryDependencies += akka
    )
  ) dependsOn (rdf, n3, rdfTestSuite % "test")
  
  lazy val sesame = Project(
    id = "sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameDeps ++ testDeps
  ) dependsOn (rdf, n3, rdfTestSuite % "test")
  
  lazy val n3 = Project(
    id = "n3",
    base = file("n3"),
    settings = buildSettings ++ jenaDeps ++ n3Deps
  ) dependsOn (rdf)
  
  lazy val n3TestSuite = Project(
    id = "n3-test-suite",
    base = file("n3-test-suite"),
    settings = buildSettings ++ testsuiteDeps
  ) dependsOn (n3, jena % "test", sesame % "test", simpleRdf % "test")

  lazy val linkedData = Project(
    id = "linked-data",
    base = file("linked-data"),
    settings = buildSettings ++ Seq(
      libraryDependencies += asyncHttpClient)
  ) dependsOn (rdf, sesame, jena)

}

