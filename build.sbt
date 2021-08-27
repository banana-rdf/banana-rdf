val Scala3Version = "3.0.1"

val munit = "org.scalameta" %% "munit" % "0.7.28" % Test

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-simple",
    version := "0.1.0",

    scalaVersion := Scala3Version,
    libraryDependencies ++= Seq(
      "org.apache.jena" % "apache-jena-libs" % "4.1.0",
      munit
    )

  )
