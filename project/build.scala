import sbt._
import sbt.Keys._
import org.ensime.sbt.Plugin.Settings.ensimeConfig
import org.ensime.sbt.util.SExp._
import com.typesafe.sbtscalariform.ScalariformPlugin._
import scalariform.formatter.preferences._

object BuildSettings {

  val logger = ConsoleLogger()

  val buildSettings = Defaults.defaultSettings ++  defaultScalariformSettings ++ Seq (
    organization := "org.w3",
//    version      := "0.3-SNAPSHOT",
    version      := "x14-SNAPSHOT",
    scalaVersion := "2.10.0-RC5",
    javacOptions ++= Seq("-source","1.7", "-target","1.7"),

    parallelExecution in Test := false,
    testOptions in Test += Tests.Argument("""stdout(config="durations")"""),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize", "-feature", "-language:implicitConversions,higherKinds", "-Xmax-classfile-name", "140"),
    resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/",
    resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    ensimeConfig := sexp(
      key(":compiler-args"), sexp("-Ywarn-dead-code", "-Ywarn-shadowing"),
      key(":formatting-prefs"), sexp(
        key(":rewriteArrowSymbols"), true,
        key(":doubleIndentClassDeclaration"), true
      )
    ),
    licenses := Seq("W3C License" -> url("http://opensource.org/licenses/W3C")),
    homepage := Some(url("https://github.com/w3c/banana-rdf")),
    publishTo <<= version { (v: String) =>
      //eg: export SBT_PROPS=-Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/
      val nexus = "https://oss.sonatype.org/"
      val other = Option(System.getProperty("banana.publish")).map(_.split(":"))
      if (v.trim.endsWith("SNAPSHOT")) {
        val repo = other.map(p=>Resolver.ssh("banana.publish specified server", p(0), p(1)+"snapshots"))
        repo.orElse(Some("snapshots" at nexus + "content/repositories/snapshots"))
      } else {
        val repo = other.map(p=>Resolver.ssh("banana.publish specified server", p(0), p(1)+"resolver"))
        repo.orElse(Some("releases" at nexus + "service/local/staging/deploy/maven2"))
      }
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
      </developers>
    )
  )

  val jenaTestWIPFilter = Seq (
    testOptions in Test += Tests.Argument("exclude(org.w3.banana.jenaWIP)")
  )

  val sesameTestWIPFilter = Seq (
    testOptions in Test += Tests.Argument("exclude(org.w3.banana.sesameWIP)")
  )

}

object BananaRdfBuild extends Build {

  import BuildSettings._
  
//  import com.typesafe.sbteclipse.plugin.EclipsePlugin._

  val scalaActors = "org.scala-lang" % "scala-actors" % "2.10.0-RC5"

  val scalaIoCore = "com.github.scala-incubator.io" % "scala-io-core_2.10.0-RC1" % "0.4.1"
  val scalaIoFile = "com.github.scala-incubator.io" % "scala-io-file_2.10.0-RC1" % "0.4.1"

  val akka = "com.typesafe.akka" % "akka-actor_2.10.0-RC5" % "2.1.0-RC6"

  val akkaTransactor = "com.typesafe.akka" % "akka-transactor_2.10.0-RC5" % "2.1.0-RC6"

  val scalaStm = "org.scala-tools" % "scala-stm_2.10.0-RC5" % "0.6"

  val asyncHttpClient = "com.ning" % "async-http-client" % "1.8.0-SNAPSHOT"

//  val scalaz = "org.scalaz" % "scalaz-core_2.10.0-M7" % "7.0.0-M3" from "http://jay.w3.org/~bertails/jar/scalaz-core_2.10.0-M7-7.0.0-M3.jar"
  val scalaz = "org.scalaz" % "scalaz-core_2.10.0-RC5" % "7.0-SNAPSHOT" // from "http://repo.typesafe.com/typesafe/releases/org/scalaz/scalaz-core_2.10.0-M6/7.0.0-M2/scalaz-core_2.10.0-M6-7.0.0-M2.jar"

  val jodaTime = "joda-time" % "joda-time" % "2.1"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val jodatimeDeps = Seq(
    libraryDependencies += jodaTime % "provided",
    libraryDependencies += jodaConvert % "provided")

  val scalatest = "org.scalatest" % "scalatest_2.10.0-RC5" % "2.0.M5-B1"
  
  val testsuiteDeps =
    Seq(
      libraryDependencies += scalaActors,
      libraryDependencies += scalatest
    )

  val iterateeDeps = "play" % "play-iteratees_2.10" % "2.1-12142012"

  val testDeps =
    Seq(
      libraryDependencies += scalaActors % "test",
      libraryDependencies += scalatest % "test"
    )
  
  val jenaDeps =
    Seq(
      resolvers += "apache-repo-releases" at "http://repository.apache.org/content/repositories/releases/",
      libraryDependencies += "org.apache.jena" % "jena-arq" % "2.9.1" excludeAll(ExclusionRule(organization = "org.slf4j")),
      libraryDependencies += "org.apache.jena" % "jena-tdb" % "0.9.1" excludeAll(ExclusionRule(organization = "org.slf4j")),
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided",
      libraryDependencies += "com.fasterxml" % "aalto-xml" % "0.9.7"
  )
  
  val sesameDeps =
    Seq(
      resolvers += "sesame-repo-releases" at "http://repo.aduna-software.org/maven2/releases/",
      libraryDependencies += "org.openrdf.sesame" % "sesame-sail-memory" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-sail-nativerdf" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryparser-sparql" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-repository-sail" % "2.6.10")

  val pub = TaskKey[Unit]("pub")

//  lazy val full = {
//    def sxrOptions(baseDir: File, sourceDirs: Seq[Seq[File]]): Seq[String] = {
//      val xplugin = "-Xplugin:" + (baseDir / "lib" / "sxr_2.9.0-0.2.7.jar").asFile.getAbsolutePath
//      val baseDirs = sourceDirs.flatten
//      val sxrBaseDir = "-P:sxr:base-directory:" + baseDirs.mkString(":")
//      Seq(xplugin, sxrBaseDir)
//    }
//
//    val projects = Seq(rdf, jena, sesame)
//    val allSources           = TaskKey[Seq[Seq[File]]]("all-sources")
//    val allSourceDirectories = SettingKey[Seq[Seq[File]]]("all-source-directories")
//
//    Project(
//      id = "full",
//      base = file("full"),
//      dependencies = Seq(rdf, jena, sesame),
//      settings     = buildSettings ++ Seq(
//        allSources           <<= projects.map(sources in Compile in _).join, // join: Seq[Task[A]] => Task[Seq[A]]
//        allSourceDirectories <<= projects.map(sourceDirectories in Compile in _).join,
//
//        // Combine the sources of other modules to generate Scaladoc and SXR annotated sources
//        (sources in Compile) <<= (allSources).map(_.flatten),
//
//        // Avoid compiling the sources here; we just are after scaladoc.
//        (compile in Compile) := inc.Analysis.Empty,
//
//        // Include SXR in the Scaladoc Build to generated HTML annotated sources.
//        scalacOptions in (Compile, doc) <++= (baseDirectory, allSourceDirectories).map {
//          (bd, asd) => sxrOptions(bd, asd)
//        }
//      )
//    )
//  }

  
  lazy val banana = Project(
    id = "banana",
    base = file("."),
    settings = buildSettings ++ Seq(
//      EclipseKeys.skipParents in ThisBuild := false,
      pub := (),
      pub <<= pub.dependsOn(publish in rdf, publish in jena, publish in sesame)),
    aggregate = Seq(
      rdf,
      rdfTestSuite,
      jena,
      sesame,
      plantain
//      ldp,
      /*full*/))
  
  lazy val rdf = Project(
    id = "banana-rdf",
    base = file("rdf"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka,
      libraryDependencies += scalaz,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert,
      publishMavenStyle := true
    )
  )

  lazy val rdfTestSuite = Project(
    id = "banana-rdf-test-suite",
    base = file("rdf-test-suite"),
    settings = buildSettings ++ testsuiteDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka,
      libraryDependencies += jodaTime,
      libraryDependencies += jodaConvert
    )
  ) dependsOn (rdf)

  lazy val jena = Project(
    id = "banana-jena",
    base = file("jena"),
    settings = buildSettings ++ jenaTestWIPFilter ++ jenaDeps ++ testDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka
    )
  ) dependsOn (rdf, rdfTestSuite % "test")
  
  lazy val sesame = Project(
    id = "banana-sesame",
    base = file("sesame"),
    settings = buildSettings ++ sesameTestWIPFilter ++ sesameDeps ++ testDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka
    )
  ) dependsOn (rdf, rdfTestSuite % "test")

  lazy val plantain = Project(
    id = "plantain",
    base = file("plantain"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka,
      libraryDependencies += akkaTransactor,
      libraryDependencies += iterateeDeps,
      resolvers += "sesame-repo-releases" at "http://repo.aduna-software.org/maven2/releases/",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryalgebra-evaluation" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryparser-sparql" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-queryresultio-sparqljson" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-turtle" % "2.6.10",
      libraryDependencies += "org.openrdf.sesame" % "sesame-rio-rdfxml" % "2.6.10",
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
    )
  ) dependsOn (rdf, rdfTestSuite % "test")

  lazy val ldp = Project(
    id = "ldp",
    base = file("ldp"),
    settings = buildSettings ++ testDeps ++ Seq(
      libraryDependencies += scalaStm
    )
  ) dependsOn (rdf, jena % "test", sesame % "test")

  
}

