import bintray.Keys._
import sbt.KeyRanks._
import sbtrelease._

bintrayPublishSettings

repository in bintray := "banana-rdf"

bintrayOrganization in bintray := Some("banana-rdf")

publishMavenStyle := false

publishArtifact in Test := false
