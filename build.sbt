import sbt.Keys.{publishMavenStyle, _}
import sbt.{url, _}
import Dependencies._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}


//sbt -Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/
//sbt -Dbanana.publish=bintray
lazy val publicationSettings = {
  val pubre = """([^:]+):([^:]+)""".r
  Option(System.getProperty("banana.publish")) match {
    case Some("bintray") | None => Seq(
      sonatypeProfileName := "net.bblfish",
      publishTo := sonatypePublishToBundle.value,

      // To sync with Maven central, you need to supply the following information:
      publishMavenStyle := true,

      // Open-source license of your choice
      licenses +=("W3C", url("http://opensource.org/licenses/W3C")),
      homepage := Some(url("https://github.com/banana-rdf/banana-rdf")),
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/banana-rdf/banana-rdf"),
          "scm:git@github.com:banana-rdf/banana-rdf.git"
        )
      ),
      developers := List(
        Developer(id="bblfish", name="Henry Story", email="henry.story@bblfish.net", url=url("https://bblfish.net")),
        Developer(id="bertails", name="Alexandre Bertails", email="alexandre@bertails.org", url=url("http://www.bertails.org/"))
      )
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
  organization := "net.bblfish.rdf",
  scalaVersion := "3.0.0-RC2",
  resolvers += "apache-repo-releases" at "https://repository.apache.org/content/repositories/releases/",
  fork := false,
  Test / parallelExecution := false,
  offline := true,
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions,higherKinds"),
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
    libraryDependencies ++= Seq(jodaTime, jodaConvert),
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
    libraryDependencies ++= Seq(akkaHttpCore, rdf4jRioTurtle, jsonldJava)
  )
  .settings(name := "banana-plantain")
  .dependsOn(rdf, ntriples, rdfTestSuite % "test->compile")

lazy val plantainJS = plantain.js
lazy val plantainJVM = plantain.jvm.settings(
  libraryDependencies += akka
)

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
      slf4jNop,
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

