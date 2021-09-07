import sbt._

object Dependencies {

	/**
	 * jena
	 * @see https://jena.apache.org/
	 * @see https://repo1.maven.org/maven2/org/apache/jena
	 */
	val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "4.1.0"

	val munit = "org.scalameta" %% "munit" % "0.7.28" % Test

}
