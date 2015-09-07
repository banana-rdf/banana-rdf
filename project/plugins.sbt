//logLevel := Level.Debug

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("com.github.inthenow" % "sbt-scalajs" % "0.6.2")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.7.5") //to reason about dependencies visually

dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.3"
