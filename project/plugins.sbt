//selenium testing
libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "1.1.1"

// problem with guava clashses when using selenium for in browser tests
// this seems to fix it. todo: remove later
//libraryDependencies += "com.google.guava" % "guava" % "31.0.1-jre"

//for sbt-typelevel when using SNAPSHOTS
//resolvers += Resolver.sonatypeRepo("snapshots")

/** allows one to access JS npm distributions with the ease with which one can work with maven
  * @see
  *   https://search.maven.org/search?q=a:sbt-scalajs-bundler
  * @see
  *   https://scalacenter.github.io/scalajs-bundler/getting-started.html
  * @see
  *   https://github.com/scalacenter/scalajs-bundler
  * @see
  *   https://scalacenter.github.io/scalajs-bundler/
  */
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.0")

// https://typelevel.org/sbt-typelevel/index.html
addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.5.0-M5")
// http://www.scala-js.org/doc/tutorial/basic/
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.11.0")
// https://github.com/sbt/sbt-buildinfo
// addSbtPlugin("com.eed3si9n"  % "sbt-buildinfo" % "0.11.0")
