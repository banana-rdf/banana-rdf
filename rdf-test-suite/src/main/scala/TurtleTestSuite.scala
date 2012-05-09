package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._

import scalaz.Validation
import scalaz.Validation._

abstract class TurtleTestSuite[Rdf <: RDF](val ops: RDFOperations[Rdf]) extends WordSpec with MustMatchers {
  
  val reader: RDFReader[Rdf, Turtle]
  val writer: TurtleWriter[Rdf]
  val iso: GraphIsomorphism[Rdf]
  
  import iso._
  
  import org.scalatest.matchers.{BeMatcher, MatchResult}
  
//  def beIsomorphicWithGeneral[M <: RDFModule](m: M)(iso: GraphIsomorphism[m.type])(right: m.Graph): BeMatcher[m.Graph] =
//    new BeMatcher[m.Graph] {
//      def apply(left: m.Graph) = MatchResult(
//        iso.isomorphism(left, right),
//        "left graph is isomorphic to right graph",
//        "%s not isomorphic with %s" format (left.toString, right.toString)
//      )
//    }
//  
//  def isomorphicWith(right: m.Graph): BeMatcher[m.Graph] = beIsomorphicWithGeneral(m)(iso)(right)

  import ops._
  
  def graphBuilder(prefix: Prefix[Rdf]) = {
    val ntriples = prefix("ntriples/")
    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
    val dave = "Dave Beckett".§
    val art = "Art Barstow".§
    val w3org = IRI("http://www.w3.org/")
    Graph(
      (ntriples, creator, dave),
      (ntriples, creator, art),
      (ntriples, publisher, w3org)
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
    graph.size must equal (77)
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
  
  "works with relative uris" in {
    val bar = for {
      turtleString <- writer.asString(referenceGraph, rdfCore)
      computedFooGraph <- reader.read(turtleString, foo)
    } yield computedFooGraph
    val g: Rdf#Graph = bar.fold( t => throw t, g => g )
    assert(fooGraph isIsomorphicWith g)
  }
  
}
