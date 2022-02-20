import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport.*
import sbt.*

object Dependencies {

  lazy val sonatypeSNAPSHOT: MavenRepository =
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

  object Ver {
    val scala3 = "3.1.1"
  }

  /** Jena
    *
    * @see
    *   https://jena.apache.org/
    * @see
    *   https://repo1.maven.org/maven2/org/apache/jena
    */
  val jenaLibs = "org.apache.jena" % "apache-jena-libs" % "4.2.0"

  object fish {
    val rdf_model_js = Def.setting("net.bblfish.rdf" %%% "rdf-model-js" % "0.2-dbfa81d-SNAPSHOT")
  }

  object TestLibs {
    /* @see munit docs https://scalameta.org/munit/ */
    val munit = Def.setting("org.scalameta" %%% "munit" % "1.0.0-M1")
    // https://github.com/com-lihaoyi/utest
    val utest = Def.setting("com.lihaoyi" %%% "utest" % "0.7.10")
    /* @see scalatest docs https://www.scalatest.org/install */
    val scalatest = Def.setting("org.scalatest" %%% "scalatest" % "3.2.11")
    val scalatic  = "org.scalactic" %% "scalactic" % "3.2.11"
  }

  /** RDF4J
    *
    * @see
    *   https://www.rdf4j.org/
    * @see
    *   https://repo1.maven.org/maven2/org/eclipse/rdf4j/
    */
  object RDF4J {
    val Version             = "3.7.4"
    lazy val QueryAlgebra   = "org.eclipse.rdf4j" % "rdf4j-queryalgebra-evaluation"  % Version
    lazy val QueryParser    = "org.eclipse.rdf4j" % "rdf4j-queryparser-sparql"       % Version
    lazy val QueryResult    = "org.eclipse.rdf4j" % "rdf4j-queryresultio-sparqljson" % Version
    lazy val RioTurtle      = "org.eclipse.rdf4j" % "rdf4j-rio-turtle"               % Version
    lazy val RioRdfxml      = "org.eclipse.rdf4j" % "rdf4j-rio-rdfxml"               % Version
    lazy val RioJsonLd      = "org.eclipse.rdf4j" % "rdf4j-rio-jsonld"               % Version
    lazy val SailMemory     = "org.eclipse.rdf4j" % "rdf4j-sail-memory"              % Version
    lazy val SailNativeRdf  = "org.eclipse.rdf4j" % "rdf4j-sail-nativerdf"           % Version
    lazy val RepositorySail = "org.eclipse.rdf4j" % "rdf4j-repository-sail"          % Version
  }

  /** jsonld-java
    *
    * @see
    *   https://github.com/jsonld-java/jsonld-java
    * @see
    *   https://repo.typesafe.com/typesafe/snapshots/com/github/jsonld-java/jsonld-java-tools
    */
  val jsonldJava = "com.github.jsonld-java" % "jsonld-java" % "0.13.3"

  /** slf4j-nop. Test dependency for logging.
    * @see
    *   https://www.slf4j.org
    */
  val slf4jNop = "org.slf4j" % "slf4j-nop" % "1.7.32" % Test

//	val `rdflib-types` = "org.scala-js" %%% "rdflib-types" % "0.1-SNAPSHOT"

  object typelevel {
    val catsCore = Def.setting("org.typelevel" %%% "cats-core" % "2.6.1")
  }

}
