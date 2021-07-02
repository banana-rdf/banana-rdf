package org.w3.banana.io

import org.w3.banana._
import scala.util._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io._

class NTriplesWriterTestSuite[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  writer: RDFWriter[Rdf, Try, NTriples]
) extends AnyWordSpec with Matchers {

  import ops._

  val foaf = FOAFPrefix[Rdf]

  val bblfish = "http://bblfish.net/people/henry/card#me"
  val name = "Henry Story"

  val typ = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"

  def ntparser(ntstring: String, skip: Boolean = false) =
    new NTriplesParser(new StringReader(ntstring), skip)

  import NTriplesParser.toGraph
  "Ntriplets writer " should  {
    "write one triplet" in {

      val g = Graph(Triple(URI(bblfish), rdf.`type`, foaf.Person))
      val str = writer.asString(g, base = Some("http://example")).get
      val graphTry = toGraph(ntparser(str))
      assert(graphTry.get isIsomorphicWith g)
    }

    "write more triplets" in {
      //TODO: rewrite with random triplets generators in future
      val g = Graph(
        Triple(URI(bblfish), foaf.name, Literal(name)),
        Triple(URI(bblfish), foaf.knows, BNode("betehess")),
        Triple(BNode("betehess"), foaf.homepage, URI("http://bertails.org/"))
      )
      val str = writer.asString(g, base = Some("http://example")).get
      val graphTry = toGraph(ntparser(str))
      assert(graphTry.get isIsomorphicWith g)
    }

  }

}
