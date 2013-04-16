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
    version      := "2013_02_21-SNAPSHOT",
    scalaVersion := "2.10.0",
    javacOptions ++= Seq("-source","1.7", "-target","1.7"),
    fork := false,
    parallelExecution in Test := false,
    offline := true,
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
        <developer>
          <id>bblfish</id>
          <name>Henry Story</name>
          <url>http://bblfish.net/</url>
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
  
  val scalaActors = "org.scala-lang" % "scala-actors" % "2.10.0"

  val scalaIoCore = "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2"
  val scalaIoFile = "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

  val akka = "com.typesafe.akka" %% "akka-actor" % "2.1.0"
  val akkaTransactor = "com.typesafe.akka" %% "akka-transactor" % "2.1.0"

//  val scalaStm = "org.scala-tools" %% "scala-stm" % "0.6"

  val asyncHttpClient = "com.ning" % "async-http-client" % "1.7.12"

  val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.0-RC1"

  val jodaTime = "joda-time" % "joda-time" % "2.1"
  val jodaConvert = "org.joda" % "joda-convert" % "1.2"

  val jodatimeDeps = Seq(
    libraryDependencies += jodaTime % "provided",
    libraryDependencies += jodaConvert % "provided")

  val scalatest = "org.scalatest" %% "scalatest" % "2.0.M6-SNAP8"
  
  val testsuiteDeps =
    Seq(
      libraryDependencies += scalaActors,
      libraryDependencies += scalatest
    )

  val iterateeDeps = "play" %% "play-iteratees" % "2.1.0"
  val playDeps = "play" %% "play" % "2.1.0"

  val reactiveMongo = "org.reactivemongo" %% "play2-reactivemongo" % "0.9-SNAPSHOT" excludeAll(ExclusionRule(organization = "io.netty"), ExclusionRule(organization = "play"))

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

  val sesameVersion = "2.6.10"
  
  val sesameCoreDeps =
    Seq(
      resolvers += "sesame-repo-releases" at "http://repo.aduna-software.org/maven2/releases/",
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
      plantain,
      examples))
  
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
    settings = buildSettings ++ testDeps ++ sesameCoreDeps ++ Seq(
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
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
      libraryDependencies += scalaIoCore,
      libraryDependencies += scalaIoFile,
      libraryDependencies += akka,
      libraryDependencies += akkaTransactor,
      libraryDependencies += iterateeDeps,
      libraryDependencies += reactiveMongo,
      libraryDependencies += playDeps,
      libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
      libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
    )
  ) dependsOn (plantain, rdfTestSuite % "test")

  lazy val ldp = Project(
    id = "ldp",
    base = file("ldp"),
    settings = buildSettings ++ testDeps ++ sesameCoreDeps ++ Seq(
        libraryDependencies += scalaIoCore,
        libraryDependencies += scalaIoFile,
        libraryDependencies += akka,
        libraryDependencies += asyncHttpClient,
        libraryDependencies += akkaTransactor,
        libraryDependencies += scalaz,
        libraryDependencies += iterateeDeps,
        libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7" % "provided",
        libraryDependencies += "log4j" % "log4j" % "1.2.16" % "provided"
    )
  ) dependsOn (plantain, rdfTestSuite % "test")

  
}

