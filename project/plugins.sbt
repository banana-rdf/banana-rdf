/**
 * ScalaJS cross project sbt plugin
 * @see http://www.scala-js.org/doc/project/cross-build.html
 * @see https://github.com/portable-scala/sbt-crossproject
 */
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.7.1")

/**
 * allows one to access JS npm distributions with the ease with which one can work with maven
 * @see https://search.maven.org/search?q=a:sbt-scalajs-bundler
 * @see https://scalacenter.github.io/scalajs-bundler/getting-started.html
 */
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.0-RC1")
addSbtPlugin("ch.epfl.scala" % "sbt-web-scalajs-bundler" % "0.21.0-RC1")

/**
 * ScalablyTyped plugin: helps convert typescript to scala-js facades
 * https://scalablytyped.org/docs/readme.html
 */
//addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta36")