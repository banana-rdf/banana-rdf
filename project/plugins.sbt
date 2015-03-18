//logLevel := Level.Debug

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

addSbtPlugin("com.github.inthenow" % "sbt-scalajs" % "0.6.2")

<<<<<<< HEAD
=======
dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.3"

>>>>>>> 963fc71... updated dependencies + movement to CommonBuild to fix the build issue
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")

dependencyOverrides += "org.scala-js" % "sbt-scalajs" % "0.6.3"
