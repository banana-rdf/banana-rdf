import sbt.KeyRanks._
import sbtrelease._
import ReleaseStateTransformations._
import scala.xml.NodeSeq

// Setup repositories
val johnsonRepo = SettingKey[Option[Resolver]]("johnsonRepo", "publish to johnsonRepo.", ASetting)

johnsonRepo := {
  val nexus = "http://johnsonusm.com:8020/nexus/"
  if (isSnapshot.value)
    Some("Sonatype Snapshots" at nexus + "content/repositories/snapshots/")
  else
    Some("Sonatype Snapshots" at nexus + "content/repositories/releases/")
}

val johnsonRepoPomExtra = SettingKey[NodeSeq]("johnsonRepoPomExtra", "Extra XML for johnsonRepo.", BSetting)

johnsonRepoPomExtra := <url>https://github.com/InTheNow</url>
  <developers>
    <developer>
      <id>Alistair</id>
      <name>Johnson</name>
      <url>https://github.com/InTheNow</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:InTheNow/sbt-scalajs.git</url>
    <connection>git@github.com:InTheNow/sbt-scalajs.git</connection>
  </scm>

pomIncludeRepository := { _ => false}

// Sbt-Release related
val checkSnapshotVersion: ReleaseStep = ReleaseStep(action = st => {
  import Utilities._
  val extracted = Project.extract(st)
  val currentV = extracted.get(version)
  if (!currentV.endsWith("-SNAPSHOT")) {
    sys.error(s"Aborting release as this is not a snapshot version: $currentV .")
  }
  SimpleReader.readLine(s"Do you want to release snapshot $currentV (y/n)? [y] ") match {
    case Yes() | Some("") => // carry on
    case _ => sys.error("Aborting release due to snapshot version not accepted.")
  }
  st
})

val checkCorrectRepo: ReleaseStep = ReleaseStep(action = st => {
  import Utilities._
  val extracted = Project.extract(st)
  val repo = extracted.get(publishTo).get
  SimpleReader.readLine(s"Do you want to use repository $repo (y/n)? [y] ") match {
    case Yes() | Some("") => // carry on
    case _ => sys.error("Aborting release as repository not accepted.")
  }
  st
})

lazy val snapshotReleaseProcess = Seq[ReleaseStep](
  checkSnapshotDependencies,
  checkCorrectRepo,
  checkSnapshotVersion,
  runClean,
  runTest,
  pushChanges,
  publishArtifacts
)

lazy val  pushToForkProcess = Seq[ReleaseStep](
  checkSnapshotDependencies,
  runClean,
  runTest,
  pushChanges
)

lazy val printInfo = taskKey[Unit]("Prints Info")

printInfo := println(s"${publishTo.value} ${publishMavenStyle.value} ${releaseSettings.toString} ${pomExtra.value}")


// Final settings to use

publishMavenStyle := true

publishTo := johnsonRepo.value

pomExtra := johnsonRepoPomExtra.value

ReleaseKeys.releaseProcess := snapshotReleaseProcess
//ReleaseKeys.releaseProcess := pushToForkProcess
