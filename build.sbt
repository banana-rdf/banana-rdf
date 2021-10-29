import Dependencies.{TestLibs, Ver, jenaLibs, typelevel}
import org.scalajs.linker.interface.ModuleKind.ESModule
import org.scalajs.linker.interface.OutputPatterns
import sbt.Keys.description
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

scalaVersion := Ver.scala3

ThisBuild / homepage      := Some(url("https://github.com/banana-rdf/banana-rdf"))
ThisBuild / licenses      += ("MIT", url("https://opensource.org/licenses/mit-license.php"))
ThisBuild / organization  := "org.scala-js"
ThisBuild / shellPrompt   := ((s: State) => Project.extract(s).currentRef.project + "> ")
ThisBuild / versionScheme := Some("early-semver")

val scala3jvmOptions =  Seq(
	// "-classpath", "foo:bar:...",         // Add to the classpath.
	//"-encoding", "utf-8",                // Specify character encoding used by source files.
	"-deprecation",                      // Emit warning and location for usages of deprecated APIs.
	"-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
	"-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
	//"-explain",                          // Explain errors in more detail.
	//"-explain-types",                    // Explain type errors in more detail.
	"-indent",                           // Together with -rewrite, remove {...} syntax when possible due to significant indentation.
	// "-no-indent",                        // Require classical {...} syntax, indentation is not significant.
//	"-rewrite",                          // Attempt to fix code automatically. Use with -indent and ...-migration.
//	"-source", "future-migration",
	"-new-syntax",                       // Require `then` and `do` in control expressions.
	// "-old-syntax",                       // Require `(...)` around conditions.
	// "-language:Scala2",                  // Compile Scala 2 code, highlight what needs updating
	//"-language:strictEquality",          // Require +derives Eql+ for using == or != comparisons
	// "-scalajs",                          // Compile in Scala.js mode (requires scalajs-library.jar on the classpath).
	"-source:future",                       // Choices: future and future-migration. I use this to force future deprecation warnings, etc.
	// "-Xfatal-warnings",                  // Fail on warnings, not just errors
	// "-Xmigration",                       // Warn about constructs whose behavior may have changed since version.
	// "-Ysafe-init",                       // Warn on field access before initialization
	"-Yexplicit-nulls"                  // For explicit nulls behavior.
	)

val scala3jsOptions =  Seq(
	// "-classpath", "foo:bar:...",         // Add to the classpath.
	//"-encoding", "utf-8",                // Specify character encoding used by source files.
	"-deprecation",                      // Emit warning and location for usages of deprecated APIs.
	"-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
	"-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
	//"-explain",                          // Explain errors in more detail.
	//"-explain-types",                    // Explain type errors in more detail.
	"-indent",                           // Together with -rewrite, remove {...} syntax when possible due to significant indentation.
	// "-no-indent",                        // Require classical {...} syntax, indentation is not significant.
	"-new-syntax",                       // Require `then` and `do` in control expressions.
	// "-old-syntax",                       // Require `(...)` around conditions.
	// "-language:Scala2",                  // Compile Scala 2 code, highlight what needs updating
	//"-language:strictEquality",          // Require +derives Eql+ for using == or != comparisons
	// "-rewrite",                          // Attempt to fix code automatically. Use with -indent and ...-migration.
	// "-scalajs",                          // Compile in Scala.js mode (requires scalajs-library.jar on the classpath).
	"-source:future",                       // Choices: future and future-migration. I use this to force future deprecation warnings, etc.
	// "-Xfatal-warnings",                  // Fail on warnings, not just errors
	// "-Xmigration",                       // Warn about constructs whose behavior may have changed since version.
	// "-Ysafe-init",                       // Warn on field access before initialization
	"-Yexplicit-nulls"                  // For explicit nulls behavior.
)


lazy val commonSettings = Seq(
	name := "banana-rdf",
	version := "0.9-SNAPSHOT",
	description := "RDF framework for Scala",
	startYear := Some(2012),
	scalaVersion := Ver.scala3,
	updateOptions := updateOptions.value.withCachedResolution(true) //to speed up dependency resolution
)

lazy val rdf = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("rdf"))
	.settings(commonSettings: _*)
	.settings(
		name := "banana-rdf",
		libraryDependencies ++= Seq(typelevel.catsCore.value),
	)
	.jvmSettings(
		scalacOptions ++= scala3jvmOptions
	)
	.jsSettings(
		scalacOptions ++= scala3jsOptions
	)

lazy val rdfJVM = rdf.jvm
lazy val rdfJS = rdf.js

lazy val ntriples = crossProject(JVMPlatform,JSPlatform)
	.crossType(CrossType.Full)
	.settings(commonSettings: _*)
	.in(file("ntriples"))
	.dependsOn(rdf)
	.jvmSettings(
		scalacOptions ++= scala3jvmOptions,
	//	scalacOptions += "-rewrite"
	)
	.jsSettings(
		scalacOptions ++= scala3jsOptions,
	//	scalacOptions += "-rewrite"
	)

lazy val ntriplesJVM = ntriples.jvm
lazy val ntriplesJS = ntriples.js

lazy val jena = project.in(file("jena"))
	.settings(commonSettings: _*)
	.settings(
		name := "banana-jena",
		scalacOptions ++= scala3jvmOptions,
		Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,
		libraryDependencies ++= Seq(jenaLibs) //, slf4jNop, aalto )
	)
	.dependsOn(rdfJVM, rdfTestSuiteJVM % "test->compile", ntriplesJVM) //, ntriplesJVM, rdfTestSuiteJVM % "test->compile")

import Dependencies.{RDF4J => rj}
lazy val rdf4j = project.in(file("rdf4j"))
	.settings(commonSettings: _*)
	.settings(
		name := "banana-rdf4j",
		scalacOptions ++= scala3jvmOptions,
		libraryDependencies ++= Seq(
			rj.QueryAlgebra,
			rj.QueryParser,
			rj.QueryResult,
			rj.RioTurtle,
			rj.RioRdfxml,
			rj.RioJsonLd,
			rj.SailMemory,
			rj.SailNativeRdf,
			rj.RepositorySail,
			Dependencies.slf4jNop,
			Dependencies.jsonldJava
		)
	).dependsOn(rdfJVM, rdfTestSuiteJVM % "test->compile") //ntriplesJVM,

lazy val rdflibJS =  project.in(file("rdflibJS"))
//	.enablePlugins(ScalaJSPlugin)
	.enablePlugins(ScalaJSBundlerPlugin)
	//	.enablePlugins(WebScalaJSBundlerPlugin)
	//documentation here: https://scalablytyped.org/docs/library-developer
	// call stImport in sbt to generate new sources
	//.enablePlugins(ScalablyTypedConverterGenSourcePlugin)
	//	.enablePlugins(ScalablyTypedConverterPlugin)
	.settings(commonSettings: _*)
	.settings(
		name := "rdflibJS",
		useYarn := true,
		scalacOptions ++= scala3jsOptions,
		Compile / npmDependencies += "rdflib" -> "2.2.7",
		Test / npmDependencies += "rdflib" -> "2.2.7",
		resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
		libraryDependencies ++= Seq(
			"net.bblfish.rdf" %%% "rdf-model-js" % "0.1-SNAPSHOT",
		 	TestLibs.scalatest.value % Test,
//			TestLibs.utest.value % Test,
			TestLibs.munit.value % Test
		),
//		testFrameworks += new TestFramework("utest.runner.Framework"),
//		scalaJSUseMainModuleInitializer := true,
//		Compile / mainClass := Some( "org.w3.banana.rdflib.test.Test" ),
//		Compile / scalaJSLinkerConfig ~= {
//			//	nodejs needs .mjs extension. See https://www.scala-js.org/doc/project/module.html
//			_.withModuleKind(ModuleKind.ESModule)
//			// replacing CommonJSModule with what is below creates a linking problem
//			.withOutputPatterns(OutputPatterns.fromJSFile("%s.mjs"))
//		}
	).dependsOn(rdfJS, rdfTestSuiteJS % "test->compile")



//lazy val rdflibScratch =  project.in(file("rdflib.scratch"))
//	// .enablePlugins(ScalaJSBundlerPlugin)
//	//documentation here: https://scalablytyped.org/docs/library-developer
//	// call stImport in sbt to generate new sources
//	//.enablePlugins(ScalablyTypedConverterGenSourcePlugin)
//	.enablePlugins(ScalablyTypedConverterPlugin)
//	.settings(commonSettings: _*)
//	.settings(
//		name := "rdflib-scratch",
//		useYarn := true,
//		scalacOptions ++= scala3jsOptions,
//		Compile / npmDependencies += "rdflib" -> "2.2.7",
//		stUseScalaJsDom := true,
//		libraryDependencies += "org.w3" %%% "rdflib-types" % "0.1-SNAPSHOT",
//		scalaJSUseMainModuleInitializer := true,
//		Compile / mainClass := Some( "org.w3.banana.testRdfLib" ),
//	)

lazy val rdfTestSuite = crossProject(JVMPlatform, JSPlatform)
	.crossType(CrossType.Full)
	.in(file("rdf-test-suite"))
	.settings(commonSettings: _*)
	.settings(
		name := "banana-test",
		libraryDependencies ++= Seq(
			TestLibs.scalatest.value,
			TestLibs.munit.value,
			TestLibs.utest.value
			)
	//	Test / resourceDirectory  := baseDirectory.value / "src/main/resources"
	)
	.dependsOn(rdf, ntriples)
	.jvmSettings(
		scalacOptions ++= scala3jvmOptions
	)
	.jsSettings(
		scalacOptions ++= scala3jsOptions,
		Test / scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) } //required for munit to run
	)

lazy val rdfTestSuiteJVM = rdfTestSuite.jvm
lazy val rdfTestSuiteJS = rdfTestSuite.js

//lazy val scratch = crossProject(JVMPlatform,JSPlatform)
//	.crossType(CrossType.Full)
//	.in(file("scratch"))
//	.settings(commonSettings: _*)
//	.settings(
//		libraryDependencies += TestLibs.munit
//	)
//	.jvmSettings(
//		name := "scratch",
//		scalacOptions ++= scala3jvmOptions,
//		//Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.ScalaLibrary,
////		libraryDependencies ++= Seq(jenaLibs, TestLibs.munit)
//	)
//lazy val scratchJVM = scratch.jvm
//lazy val scratchJS = scratch.js
