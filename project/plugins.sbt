addSbtPlugin("org.ensime" % "ensime-sbt-cmd" % "0.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.2.0")

// addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0-SNAPSHOT")
