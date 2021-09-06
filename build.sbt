lazy val root = project
  .in(file("."))
  .settings(
	  name := "banana-play",
	  version := "0.1.0",

	  scalaVersion := Scala3Version,
	  libraryDependencies ++= Seq(
		  "org.apache.jena" % "apache-jena-libs" % "4.1.0",
		  munit
	  ),
	  scalacOptions := Seq(
		  // "-classpath", "foo:bar:...",         // Add to the classpath.
		  //"-encoding", "utf-8",                // Specify character encoding used by source files.
		  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
		  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
		  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
		  "-explain",                          // Explain errors in more detail.
		  "-explain-types",                    // Explain type errors in more detail.
		  "-indent",                           // Together with -rewrite, remove {...} syntax when possible due to significant indentation.
		  // "-no-indent",                        // Require classical {...} syntax, indentation is not significant.
		  "-new-syntax",                       // Require `then` and `do` in control expressions.
		  // "-old-syntax",                       // Require `(...)` around conditions.
		  // "-language:Scala2",                  // Compile Scala 2 code, highlight what needs updating
		  //"-language:strictEquality",          // Require +derives Eql+ for using == or != comparisons
		  // "-rewrite",                          // Attempt to fix code automatically. Use with -indent and ...-migration.
		  // "-scalajs",                          // Compile in Scala.js mode (requires scalajs-library.jar on the classpath).
		  //"-source:future",                       // Choices: future and future-migration. I use this to force future deprecation warnings, etc.
		  "-Xfatal-warnings",                  // Fail on warnings, not just errors
		  // "-Xmigration",                       // Warn about constructs whose behavior may have changed since version.
		  // "-Ysafe-init",                       // Warn on field access before initialization
		  "-Yexplicit-nulls"                  // For explicit nulls behavior.
	  )
  )
val Scala3Version = "3.0.2-RC1"
val munit = "org.scalameta" %% "munit" % "0.7.28" % Test
