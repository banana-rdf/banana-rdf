import sbt._
import sbt.Keys._

import bintray.Plugin._
import bintray.Keys._

object Publishing {

  val pomSettings = Seq(
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

  /** The env variable `banana.publish` determines how/where we publish:
    * 
    * - if absent, publish to OSS sonatype
    * - if set to "bintray", publish to the banana-rdf organization
    * - if set to a SSH URI, publish there
    * 
    * For example:
    * 
    * - `sbt -Dbanana.publish=bintray`
    * - `sbt -Dbanana.publish=bblfish.net:/home/hjs/htdocs/work/repo/`
    */
  def publicationSettings = pomSettings ++ Seq(publishArtifact in Test := false) ++ {
  
    // extractor for the components of the SSH URI
    object SshUri {
      def unapply(s: String): Option[(String, String)] = s.split(":") match {
        case Array(hostname, basePath) => Some((hostname, basePath))
        case _                         => None
      }
    }

    Option(System.getProperty("banana.publish")) match {

      case Some("bintray") =>
        bintrayPublishSettings ++ Seq(
          repository in bintray := "banana-rdf",
          bintrayOrganization in bintray := Some("banana-rdf")
        )

      case None =>
        val nexus = "https://oss.sonatype.org/"
        val publishSetting = publishTo <<= version { (v: String) =>
          if (v.trim.endsWith("SNAPSHOT"))
            Some("snapshots" at nexus + "content/repositories/snapshots")
          else
            Some("releases" at nexus + "service/local/staging/deploy/maven2")
        }
        Seq(publishSetting)

      case Some(SshUri(hostname, basePath)) =>
        val publishSetting = publishTo <<= version { (v: String) =>
          if (v.trim.endsWith("SNAPSHOT"))
            Some(Resolver.ssh("banana.publish specified server", hostname, basePath + "snapshots"))
          else
            Some(Resolver.ssh("banana.publish specified server", hostname, basePath + "resolver"))
        }
        Seq(publishSetting)

    }

  }

}
