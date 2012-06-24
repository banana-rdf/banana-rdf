package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz.Validation
import scalaz.Validation._

object JenaWIP extends Tag("org.w3.banana.jenaWIP")
object SesameWIP extends Tag("org.w3.banana.sesameWIP")

abstract class TurtleTestSuite[Rdf <: RDF]()(implicit val diesel: Diesel[Rdf])
extends WordSpec with MustMatchers {
  
  val reader: RDFReader[Rdf, Turtle]
  val writer: RDFBlockingWriter[Rdf, Turtle]
  import diesel._
  import ops._
  
  import org.scalatest.matchers.{ BeMatcher, MatchResult }
  
  def graphBuilder(prefix: Prefix[Rdf]) = {
    val ntriples = prefix("ntriples/")
    val creator = uri("http://purl.org/dc/elements/1.1/creator")
    val publisher = uri("http://purl.org/dc/elements/1.1/publisher")
    val dave = TypedLiteral("Dave Beckett")
    val art = TypedLiteral("Art Barstow")
    val w3org = uri("http://www.w3.org/")
    Graph(
      Triple(ntriples, creator, dave),
      Triple(ntriples, creator, art),
      Triple(ntriples, publisher, w3org)
    )
  }
  
  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val rdfCorePrefix = Prefix("rdf", rdfCore, ops)
  val referenceGraph = graphBuilder(rdfCorePrefix)
  
  // TODO: there is a bug in Sesame with hash uris as prefix
  val foo = "http://example.com/foo/"
  val fooPrefix = Prefix("foo", foo, ops)
  val fooGraph = graphBuilder(fooPrefix)
  
  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/src/main/resources/card.ttl")
    val graph = reader.read(file, file.toURI.toString).fold( t => throw t, g => g )
//    graph.fold( _.printStackTrace, r => println(r.size))
    graph.toIterable.size must equal (77)
  }
  
  "read simple TURTLE String" in {
    val turtleString ="""
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
 """
    val graph = reader.read(turtleString, rdfCore).fold( t => throw t, g => g )
    assert(referenceGraph isIsomorphicWith graph)

  }
  
  "write simple graph as TURTLE string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").fold( t => throw t, s => s )
    turtleString must not be ('empty)
  }
  
  "works with relative uris" taggedAs (JenaWIP) in {
    val bar = for {
      turtleString <- writer.asString(referenceGraph, rdfCore)
      computedFooGraph <- reader.read(turtleString, foo)
    } yield computedFooGraph
    val g: Rdf#Graph = bar.fold( t => throw t, g => g )
    assert(fooGraph isIsomorphicWith g)
  }
  
}
