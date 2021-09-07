import Dependencies.{jenaLibs, munit}
import sbt.Keys.description
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

lazy val commonSettings = Seq(
	  name := "banana-play",
	  version := "0.1.0",
	  description := "RDF framework for Scala",
	  startYear := Some(2012),
	  scalaVersion := "3.0.2",
	  libraryDependencies ++= Seq(
		  jenaLibs,
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
	  ),
		updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val rdf = crossProject(JVMPlatform) /*JSPlatform,*/
	.crossType(CrossType.Full)
	.in(file("rdf")) //websim api
	.settings(commonSettings: _*)
	.settings(
		name := "banana-rdf",
	)
	.jvmSettings(
		libraryDependencies ++= Seq(),
	)

lazy val rdfJVM = rdf.jvm

lazy val jena = project.in(file("jena"))
	.settings(commonSettings: _*)
	.settings(
		name := "banana-jena",
		Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,
		libraryDependencies ++= Seq(jenaLibs) //, slf4jNop, aalto )
	).dependsOn(rdfJVM) //, ntriplesJVM, rdfTestSuiteJVM % "test->compile")


