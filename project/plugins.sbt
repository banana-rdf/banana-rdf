addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")

addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.3")

/** Dotty plugin no longer needed for sbt 1.5.0  */
//addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.5.4")
/** scala3 migrate, kept here for references. Remove when migration complete */
//addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.4.0-SNAPSHOT")

//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.9")

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
  * ScalaJS cross project sbt plugin
  *
  * @see https://github.com/portable-scala/sbt-crossproject
  */
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

/**
  * jsdependencies t sbt plugin
  *
  * @see https://github.com/scala-js/jsdependencies
  */
addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

/**
  * ScalaJS sbt plugin
  *
  * @see http://www.scala-js.org/
  */

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.6.0")


/**
  * sbt dependency graph sugar
  *
  * for drawing a dependency tree
  * @see https://github.com/gilt/sbt-dependency-graph-sugar
  */
addSbtPlugin("com.gilt" % "sbt-dependency-graph-sugar" % "0.9.0")

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
// addSbtPlugin("io.get-coursier" % "sbt-coursier" % "2.0.0-RC6-8")

/**
  * sbt-updates
  *
  * for easier dependency updates monitoring
  * @see https://github.com/rtimush/sbt-updates
  */
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.5.2")

/**
 * sbtsonatype plugin used to publish artifact to maven central via sonatype nexus
 * @see https://github.com/xerial/sbt-sonatype
 */
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.7")


  /**
  * plugin used to sign the artifcat with pgp keys
  * @see https://github.com/sbt/sbt-pgp
  */
addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.1.2")
