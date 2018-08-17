addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.1")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.6")

/**
  * Bintray pluging
  *
  * @see https://github.com/softprops/bintray-sbt
  *
  * note: there seems to be a problem with this plugin, in that it does not work
  * well with other publications methods.  So we'll disable it for the moment.
  * @see https://github.com/sbt/sbt-testng/issues/4
  * @see https://github.com/typesafehub/dbuild/issues/158
  */
//addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

/**
  * ScalaJS sbt plugin
  *
  * @see http://www.scala-js.org/
  */

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.21")


/**
  * sbt dependency graph sugar
  *
  * for drawing a dependency tree
  * @see https://github.com/gilt/sbt-dependency-graph-sugar
  */
//addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.8.2")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")

/**
  * scala-style
  *
  * for Scala style checking
  * @see http://www.scalastyle.org/
  */
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")


/**
  * coursier
  *
  * better ivy alternative for dependency resolution
  * @see https://github.com/alexarchambault/coursier
  */
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC13")

/**
  * sbt-updates
  *
  * for easier dependency updates monitoring
  * @see https://github.com/rtimush/sbt-updates
  */
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.3")
