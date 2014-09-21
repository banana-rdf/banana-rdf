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
 *   run-main org.w3.banana.examples.BananaDSLApp2
 */
object BananaDSLApp2 extends App 
		with TestBananaDSL2
		with JenaModule {
  println( createGraph.toString )
}

trait TestBananaDSL2 extends RDFOpsModule {
  def createGraph() : Rdf#Graph = {
  import Ops._
  val foaf = FOAFPrefix[Rdf]
  val exampleGraph = (
    URI("betehess")
    -- foaf.name ->- "Alexandre".lang("fr")
    -- foaf.title ->- "Mr"
    -- foaf.knows ->- (
      URI("http://bblfish.net/#hjs")
      -- foaf.name ->- "Henry Story"
      -- foaf.currentProject ->- URI("http://webid.info/"))).graph
  exampleGraph
  }
}