package org.w3.banana.examples

import org.w3.banana.FOAFPrefix
import org.w3.banana.RDFModule
import org.w3.banana.RDFOpsModule
import org.w3.banana.diesel.toPointedGraphW
import org.w3.banana.jena.JenaModule

/* example creating RDF with DSL (Domain Specific Language)
 * 
 * See general explanations in IOExample.scala.
 * 
 * To run this example from sbt:
 *   project examples
 *   run-main org.w3.banana.examples.BananaDSLApp
 */
object BananaDSLApp extends App 
		with TestBananaDSL
		with JenaModule {
  println( exampleGraph.toString )
}

trait TestBananaDSL extends RDFOpsModule {
  import Ops._
  lazy val foaf = FOAFPrefix[Rdf]
  lazy val exampleGraph = (
    URI("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/"))).graph
}