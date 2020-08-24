val dottyVersion = "0.26.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dotty-simple",
    version := "0.1.0",

    scalaVersion := dottyVersion,
    libraryDependencies ++= Seq(
      "org.apache.jena" % "apache-jena-libs" % "3.15.0",
      "com.novocode" % "junit-interface" % "0.11" % "test"
    )
  )
