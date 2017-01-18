import sbt._
import sbt.Keys._
import sbtrelease.ReleasePlugin._

import scala.util.Try

object Publishing {

  val pomSettings = Seq(
    pomIncludeRepository := { _ => false},
    pomExtra :=
      <url>https://github.com/modelfabric/banana-rdf</url>
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
          <url>https://github.com/modelfabric/banana-rdf.git</url>
          <connection>scm:git:https://github.com/modelfabric/banana-rdf.git</connection>
        </scm>
    ,
    licenses +=("W3C", url("http://opensource.org/licenses/W3C"))
  )

  val snapshotRepository = Try("snapshots" at sys.env("REPOSITORY_SNAPSHOTS")).toOption
  val releaseRepository =  Try("releases"  at sys.env("REPOSITORY_RELEASES" )).toOption

  val publicationSettings = pomSettings ++ releaseSettings ++ Seq(
    publishTo := {
      if (isSnapshot.value) {
        snapshotRepository
      } else {
        releaseRepository
      }
    },
    publishMavenStyle := true,
    publishArtifact in Test := false
  )

}
