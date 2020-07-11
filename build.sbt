import sbt.Keys._
import sbt._
import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

lazy val pomSettings = Seq(
  pomIncludeRepository := { _ => false},
  pomExtra :=
    <url>https://github.com/w3c/banana-rdf</url>
      <developers>
        <developer>
          <id>betehess</id>
          <name>Alexandre Bertails</name>
          <url>http://bertails.org/</url>
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
  ,
  licenses +=("W3C", url("http://opensource.org/licenses/W3C"))
)

//sbt -Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/
//sbt -Dbanana.publish=bintray
lazy val publicationSettings = pomSettings ++ {
  val pubre = """([^:]+):([^:]+)""".r
  Option(System.getProperty("banana.publish")) match {
    case Some("bintray") | None => Seq(
// removed due to issue https://github.com/typesafehub/dbuild/issues/158
//      publishTo := {
//        val nexus = "https://oss.sonatype.org/"
//        if (isSnapshot.value)
//          Some("snapshots" at nexus + "content/repositories/snapshots")
//        else
//          Some("releases" at nexus + "service/local/staging/deploy/maven2")
//      },
//      releasePublishArtifactsAction := PgpKeys.publishSigned.value,
//      Test / publishArtifact  := false
    )
    case Some(pubre(host, path)) =>
      Seq(
        publishTo := Some(
          Resolver.ssh("banana.publish specified server",
            host,
            path + {
              if (isSnapshot.value) "snapshots" else "releases"
            }
          )
        ),
        Test / publishArtifact  := false
      )
    case other => Seq()
  }
}

lazy val commonSettings = publicationSettings ++ scalariformSettings ++ Seq(
  organization := "org.w3",
  scalaVersion := "2.12.11",
  crossScalaVersions := Seq("2.12.11"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  resolvers += "apache-repo-releases" at "https://repository.apache.org/content/repositories/releases/",
  fork := false,
  Test / parallelExecution := false,
  offline := true,
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140"),
  scalacOptions in(Compile, doc) := Seq("-groups", "-implicits"),
  description := "RDF framework for Scala",
  startYear := Some(2012),
  updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val scalariformSettings = Seq(
   scalariformAutoformat := false
)

lazy val rdf = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("rdf")) //websim api
  .settings(commonSettings: _*)
  .settings(
    name := "banana-rdf",
    libraryDependencies += scalaz.value
  )
  .jvmSettings(
    libraryDependencies ++= Seq(jodaTime, jodaConvert)
  )

lazy val rdfJS = rdf.js
lazy val rdfJVM = rdf.jvm

lazy val ntriples = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .settings(commonSettings: _*)
  .in(file("ntriples"))
  .dependsOn(rdf)

lazy val ntriplesJS = ntriples.js
lazy val ntriplesJVM = ntriples.jvm

lazy val rdfTestSuite = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("rdf-test-suite"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-test",
    libraryDependencies += scalatest.value,
    libraryDependencies += jodaTime,
    libraryDependencies += jodaConvert,
    libraryDependencies += fuseki,
    libraryDependencies += servlet,
    libraryDependencies += httpComponents,
    Test / resourceDirectory  := baseDirectory.value / "src/main/resources"
  )
  .dependsOn(rdf, ntriples)

lazy val rdfTestSuiteJVM = rdfTestSuite.jvm
lazy val rdfTestSuiteJS = rdfTestSuite.js

lazy val plantain = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("plantain"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(akkaHttpCore, rdf4jRioTurtle, jsonldJava, java8Compat)
  )
  .settings(name := "banana-plantain")
  .dependsOn(rdf, ntriples, rdfTestSuite % "test->compile")

lazy val plantainJS = plantain.js
lazy val plantainJVM = plantain.jvm

lazy val jena = Project("jena", file("jena"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-jena",
    Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,
    libraryDependencies ++= Seq(jenaLibs, slf4jNop, aalto )
  ).dependsOn(rdfJVM, ntriplesJVM, rdfTestSuiteJVM % "test->compile")

lazy val rdf4j = Project("rdf4j", file("rdf4j"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-rdf4j",
    libraryDependencies ++= Seq(
      rdf4jQueryAlgebra,
      rdf4jQueryParser,
      rdf4jQueryResult,
      rdf4jRioTurtle,
      rdf4jRioRdfxml,
      rdf4jRioJsonLd,
      rdf4jSailMemory,
      rdf4jSailNativeRdf,
      rdf4jRepositorySail,
      commonsLogging,
      jsonldJava
    )
  ).dependsOn(rdfJVM, ntriplesJVM, rdfTestSuiteJVM % "test->compile")

lazy val jsonldJS = Project("jsonld", file("jsonld.js"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-jsonld",
     jsDependencies += ProvidedJS / "jsonld.js" commonJSName "jsonld"
  ).dependsOn(rdfJS, ntriplesJS, plantainJS, rdfTestSuiteJS % "test->compile")
  .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin )

lazy val examples = Project("examples", file("misc/examples"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-examples"
  ).dependsOn(rdf4j, jena)

lazy val runExamplesStr = ";examples/runMain org.w3.banana.examples.GraphExampleWithJena" +
                          ";examples/runMain org.w3.banana.examples.GraphExampleWithRdf4j" +
                          ";examples/runMain org.w3.banana.examples.IOExampleWithJena" +
                          ";examples/runMain org.w3.banana.examples.IOExampleWithRdf4j" +
                          ";examples/runMain org.w3.banana.examples.SPARQLExampleWithJena"

name := "banana"

commonSettings

enablePlugins(ScalaUnidocPlugin)

unidocProjectFilter in ( ScalaUnidoc, unidoc ) :=
    inAnyProject -- inProjects( ntriplesJS, plantainJS, rdfJS, rdfTestSuiteJS )

addCommandAlias("validate", ";compile;test;runExamples")

addCommandAlias("runExamples", runExamplesStr)

