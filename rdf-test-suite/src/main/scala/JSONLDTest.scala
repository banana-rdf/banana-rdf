package org.w3.banana

import java.io._
import scalax.io._

import org.scalatest._
import org.scalatest.matchers.MustMatchers

import org.w3.banana.syntax._
import org.w3.banana.diesel._

abstract class JSONLDTest[Rdf <: RDF]
(readerSelector: ReaderSelector[Rdf], writerSelector: RDFWriterSelector[Rdf])(implicit ops: RDFOps[Rdf])
  extends WordSpec with MustMatchers {

  val turtleReader: RDFReader[Rdf, Turtle]
  val turtleWriter: RDFWriter[Rdf, Turtle]

  val jsonldReader: RDFReader[Rdf, JSONLD_COMPACTED]
  val jsonldWriter: RDFWriter[Rdf, JSONLD_COMPACTED]

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

  "get proper JSONLD writer by content type" in {
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

    val jsonldWriter = writerSelector(MediaRange("""application/ld+json;profile="http://www.w3.org/ns/json-ld#compacted"""")).get
    assert(jsonldWriter.syntax.mime == """application/ld+json;profile="http://www.w3.org/ns/json-ld#compacted"""")
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").get

    val jsonldGraph = jsonldReader.read(jsonld, "http://manu.sporny.org/i/public").get
    assert(referenceGraph isIsomorphicWith jsonldGraph)
  }
}
