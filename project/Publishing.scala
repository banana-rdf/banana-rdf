import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin._

import scala.util.Try

object Publishing {

  val snapshotRepository = Try("snapshots" at sys.env("REPOSITORY_SNAPSHOTS")).toOption
  val releaseRepository =  Try("releases"  at sys.env("REPOSITORY_RELEASES" )).toOption

  val jenkinsMavenSettings = Seq(
    publishMavenStyle       := true,
    publishArtifact in Test := false,
    pomIncludeRepository    := { _ => false },
    publishTo := {
      if (isSnapshot.value) {
        snapshotRepository
      } else {
        releaseRepository
      }
    },
    pomExtra := {
        <scm>
          <url>https://github.com/modelfabric/banana-rdf.git</url>
          <connection>scm:git:https://github.com/modelfabric/banana-rdf.git</connection>
        </scm>
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
    },
    licenses +=("W3C", url("http://opensource.org/licenses/W3C"))
  )
}
