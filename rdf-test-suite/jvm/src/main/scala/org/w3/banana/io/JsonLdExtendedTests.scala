package org.w3.banana.io

import java.io._

import org.scalatest._
import org.w3.banana._

import scalaz._
import scalaz.syntax._, comonad._

class JsonLdExtendedTest[Rdf <: RDF, M[+_] : Comonad](implicit
  ops: RDFOps[Rdf],
  writerSelector: RDFWriterSelector[Rdf, M],
  turtleReader: RDFReader[Rdf, M, Turtle],
  turtleWriter: RDFWriter[Rdf, M, Turtle],
  jsonldReader: RDFReader[Rdf, M, JsonLd],
  jsonldWriter: RDFWriter[Rdf, M, JsonLdCompacted]
) extends WordSpec with Matchers {

  import ops._

  def strToInput(str: String) = new ByteArrayInputStream(str.getBytes("UTF8"))

  val turtleGraph =
    """
      |@prefix ex: <http://example.com/vocab#> .
      |@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
      |
      |<http://example.com/>
      | ex:numbers "14"^^xsd:integer, "2.78E0"^^xsd:double ;
      | ex:booleans "true"^^xsd:boolean, "false"^^xsd:boolean .
    """.stripMargin

  "handle typed literals correctly" in {
    val referenceGraph = turtleReader.read(strToInput(turtleGraph), "http://manu.sporny.org/i/public").copoint
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").copoint
    val jsonldGraph = jsonldReader.read(strToInput(jsonld), "http://manu.sporny.org/i/public").copoint

    assert(referenceGraph isIsomorphicWith jsonldGraph)
  }

  "test json-ld compact writer selector" in {
    val mr = MediaRange("""application/ld+json;profile="http://www.w3.org/ns/json-ld#compacted"""")
    mr.range should be("application")
    mr.subRange should be("ld+json")
    mr.params.size should be(1)
    mr.params("profile") should be("http://www.w3.org/ns/json-ld#compacted")

    val matches = mr.matches(Syntax.JsonLdCompacted.mimeTypes.head)
    matches should be(true)

    val referenceGraph = turtleReader.read(strToInput(turtleGraph), "http://manu.sporny.org/i/public").copoint
    val jsonldWriter = writerSelector(mr).get
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").copoint
    val jsonldGraph = jsonldReader.read(strToInput(jsonld), "http://manu.sporny.org/i/public").copoint
    assert(referenceGraph isIsomorphicWith jsonldGraph)

  }

  "test json-ld expanded writer selector" in {
    val mr = MediaRange("""application/ld+json;profile="http://www.w3.org/ns/json-ld#expanded"""")
    mr.range should be("application")
    mr.subRange should be("ld+json")
    mr.params.size should be(1)
    mr.params("profile") should be("http://www.w3.org/ns/json-ld#expanded")

    val matches = mr.matches(Syntax[JsonLdExpanded].mimeTypes.head)
    matches should be(true)

    val referenceGraph = turtleReader.read(strToInput(turtleGraph), "http://manu.sporny.org/i/public").copoint
    val jsonldWriter = writerSelector(mr).get
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").copoint
    val jsonldGraph = jsonldReader.read(strToInput(jsonld), "http://manu.sporny.org/i/public").copoint
    assert(referenceGraph isIsomorphicWith jsonldGraph)
  }

  "test json-ld flattened writer selector" in {
    val mr = MediaRange("""application/ld+json;profile="http://www.w3.org/ns/json-ld#flattened"""")
    mr.range should be("application")
    mr.subRange should be("ld+json")
    mr.params.size should be(1)
    mr.params("profile") should be("http://www.w3.org/ns/json-ld#flattened")

    val matches = mr.matches(Syntax[JsonLdFlattened].mimeTypes.head)
    matches should be(true)

    val referenceGraph = turtleReader.read(strToInput(turtleGraph), "http://manu.sporny.org/i/public").copoint
    val jsonldWriter = writerSelector(mr).get
    val jsonld = jsonldWriter.asString(referenceGraph, "http://manu.sporny.org/i/public").copoint
    val jsonldGraph = jsonldReader.read(strToInput(jsonld), "http://manu.sporny.org/i/public").copoint
    assert(referenceGraph isIsomorphicWith jsonldGraph)
  }

}
