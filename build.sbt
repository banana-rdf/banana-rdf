import com.typesafe.sbt.SbtScalariform.defaultScalariformSettings
import sbt.Keys._
import sbt._
import Dependencies._
import com.typesafe.sbt.pgp.PgpKeys

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
//      publishArtifact in Test := false
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
        publishArtifact in Test := false
      )
    case other => Seq()
  }
}

lazy val commonSettings = publicationSettings ++ defaultScalariformSettings ++ Seq(
  organization := "org.w3",
  scalaVersion := "2.12.1",
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
  fork := false,
  parallelExecution in Test := false,
  offline := true,
  scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140"),
  scalacOptions in(Compile, doc) := Seq("-groups", "-implicits"),
  description := "RDF framework for Scala",
  startYear := Some(2012),
  updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val rdf = crossProject
  .crossType(CrossType.Full)
  .in(file("rdf")) //websim api
  .settings(commonSettings: _*)
  .settings(
    name := "banana-rdf",
    libraryDependencies += scalaz
  )
  .jvmSettings(
    libraryDependencies ++= Seq(jodaTime, jodaConvert)
  )

lazy val rdfJS = rdf.js
lazy val rdfJVM = rdf.jvm

lazy val ntriples = crossProject
  .crossType(CrossType.Full)
  .settings(commonSettings: _*)
  .in(file("ntriples"))
  .dependsOn(rdf)

lazy val ntriplesJS = ntriples.js
lazy val ntriplesJVM = ntriples.jvm

lazy val rdfTestSuite = crossProject
  .crossType(CrossType.Full)
  .in(file("rdf-test-suite"))
  .settings(commonSettings: _*)
  .settings(
    name := "banana-test",
    libraryDependencies += scalatest,
    libraryDependencies += jodaTime,
    libraryDependencies += jodaConvert,
    libraryDependencies += fuseki,
    libraryDependencies += fusekiServer,
    libraryDependencies += servlet,
    libraryDependencies += httpComponents,
    resourceDirectory in Test := baseDirectory.value / "src/main/resources"
  )
  .dependsOn(rdf, ntriples)

lazy val rdfTestSuiteJVM = rdfTestSuite.jvm
lazy val rdfTestSuiteJS = rdfTestSuite.js

lazy val plantain = crossProject
  .crossType(CrossType.Full)
  .in(file("plantain"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(akkaHttpCore, sesameRioTurtle, jsonldJava)
  )
  .settings(name := "banana-plantain")
  .dependsOn(rdf, ntriples, rdfTestSuite % "test->compile")

lazy val plantainJS = plantain.js
lazy val plantainJVM = plantain.jvm

lazy val jena = Project("jena", file("jena"), settings = commonSettings)
  .settings(
    name := "banana-jena",
    libraryDependencies ++= Seq(jenaLibs, commonsLogging, aalto )
  ).dependsOn(rdfJVM, ntriplesJVM, rdfTestSuiteJVM % "test->compile")

lazy val sesame = Project("sesame", file("sesame"), settings = commonSettings)
  .settings(
    name := "banana-sesame",
    libraryDependencies ++= Seq(
      sesameQueryAlgebra,
      sesameQueryParser,
      sesameQueryResult,
      sesameRioTurtle,
      sesameRioRdfxml,
      sesameSailMemory,
      sesameSailNativeRdf,
      sesameRepositorySail,
      commonsLogging,
      jsonldJava
    )
  ).dependsOn(rdfJVM, ntriplesJVM, rdfTestSuiteJVM % "test->compile")

lazy val jsonldJS = Project("jsonld", file("jsonld.js"), settings = commonSettings)
  .settings(
    name := "banana-jsonld"
  ).dependsOn(rdfJS, ntriplesJS, plantainJS, rdfTestSuiteJS % "test->compile")
  .enablePlugins(ScalaJSPlugin)

lazy val examples = Project("examples", file("misc/examples"), settings = commonSettings)
  .settings(
    name := "banana-examples"
  ).dependsOn(sesame, jena)

lazy val runExamplesStr =
  ";examples/run-main org.w3.banana.examples.GraphExampleWithJena" +
    ";examples/run-main org.w3.banana.examples.GraphExampleWithSesame" +
    ";examples/run-main org.w3.banana.examples.IOExampleWithJena" +
    ";examples/run-main org.w3.banana.examples.IOExampleWithSesame" +
    ";examples/run-main org.w3.banana.examples.SPARQLExampleWithJena"

name := "banana"

commonSettings

unidocSettings

addCommandAlias("validate", ";compile;test;runExamples")

addCommandAlias("runExamples", runExamplesStr)
