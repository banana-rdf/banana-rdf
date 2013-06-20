package org.w3.banana

import java.io._
import scalax.io._

import org.scalatest._
import org.scalatest.matchers.MustMatchers

import org.w3.banana.syntax._
import org.w3.banana.diesel._

abstract class JSONLDTest[Rdf <: RDF]()(implicit ops: RDFOps[Rdf])
  extends WordSpec with MustMatchers {

  val turtleReader: RDFReader[Rdf, Turtle]
  val turtleWriter: RDFWriter[Rdf, Turtle]

  val jsonldReader: RDFReader[Rdf, JSONLD]
  val jsonldWriter: RDFWriter[Rdf, JSONLD]

  import ops._

  "handle typed literals correctly" in {
    val turtleGraph =
      """
        |@prefix ex: <http://example.com/vocab#> .
        |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        |
        |<http://example.com/>
        |  ex:numbers "14"^^xsd:integer, "2.78E0"^^xsd:double ;
        |  ex:booleans "true"^^xsd:boolean, "false"^^xsd:boolean .
      """.stripMargin
    val referenceGraph = turtleReader.read(turtleGraph, "http://manu.sporny.org/i/public").get
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").get

    val jsonldGraph = jsonldReader.read(jsonld, "http://manu.sporny.org/i/public").get
    assert(referenceGraph isIsomorphicWith jsonldGraph)
  }
}
