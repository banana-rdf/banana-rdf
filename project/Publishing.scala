import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin.ReleaseKeys._
import sbtrelease.ReleasePlugin._
import com.typesafe.sbt.pgp.PgpKeys

object Publishing {

  val pomSettings = Seq(
    pomIncludeRepository := { _ => false},
    pomExtra :=
      <url>https://github.com/w3c/banana-rdf</url>
        <developers>
 gio         <developer>
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

  val publicationSettings = pomSettings ++ releaseSettings ++ Seq(
    publishTo := {
      val artifactory = "http://artifactory-ndc.bnymellon.net/artifactory/"
      if (isSnapshot.value) {
        Some("snapshots" at artifactory + "libs-snapshot-local")
      } else {
        Some("releases" at artifactory + "libs-release-local")
      }
    },
    publishArtifactsAction := PgpKeys.publishSigned.value,
    publishMavenStyle := true,
    publishArtifact in Test := false
  )

}
