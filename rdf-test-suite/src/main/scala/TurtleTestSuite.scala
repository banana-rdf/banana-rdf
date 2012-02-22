package org.w3.rdf

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import org.scalatest.EitherValues._


abstract class TurtleTestSuite[M <: RDFModule](val m: M) extends WordSpec with MustMatchers {
  
  val reader: TurtleReader[m.type]
  val writer: TurtleWriter[m.type]
  val iso: GraphIsomorphism[m.type]
  
  import iso._
  
  import org.scalatest.matchers.{BeMatcher, MatchResult}
  
  def beIsomorphicWithGeneral[M <: RDFModule](m: M)(iso: GraphIsomorphism[m.type])(right: m.Graph): BeMatcher[m.Graph] =
    new BeMatcher[m.Graph] {
      def apply(left: m.Graph) = MatchResult(
        iso.isomorphism(left, right),
        "left graph is isomorphic to right graph",
        "%s not isomorphic with %s" format (left.toString, right.toString)
      )
    }
  
  def isomorphicWith(right: m.Graph): BeMatcher[m.Graph] = beIsomorphicWithGeneral(m)(iso)(right)

  import m._
  
  def graphBuilder(prefix: Prefix) = {
    val ntriples = prefix("ntriples/")
    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
    val dave = "Dave Beckett".typedLiteral
    val art = "Art Barstow".typedLiteral
    val w3org = IRI("http://www.w3.org/")
    Graph(
      (ntriples, creator, dave),
      (ntriples, creator, art),
      (ntriples, publisher, w3org)
    )
  }
  
  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val rdfCorePrefix = prefixBuilder(rdfCore) _
  val referenceGraph = graphBuilder(rdfCorePrefix)
  
  // TODO: there is a bug in Sesame with hash uris as prefix
  val foo = "http://example.com/foo/"
  val fooPrefix = prefixBuilder(foo) _
  val fooGraph = graphBuilder(fooPrefix)
  
  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/src/main/resources/card.ttl")
    val graph = reader.read(file, file.toURI.toString)
//    graph.fold( _.printStackTrace, r => println(r.size))
    graph.right.value.size must equal (77)
  }
  
  "read simple TURTLE String" in {
    val turtleString ="""
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
 """
    val graph = reader.read(turtleString, rdfCore)
    val g: m.Graph = graph.right.get
    assert(referenceGraph isIsomorphicWith g)
    
  }
  
  "write simple graph as TURTLE string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/")
    turtleString.right.value must not be ('empty)
  }
  
  "works with relative uris" in {
    val turtleString = writer.asString(referenceGraph, rdfCore)
//    println("  --- "+turtleString)
    val computedFooGraph = turtleString.right flatMap { s => reader.read(s, foo) }
//    println("0 --- "+computedFooGraph)
//    println("1 --- "+fooGraph)
    val g: m.Graph = computedFooGraph.right.get
//    println("2 --- "+g)
    assert(fooGraph isIsomorphicWith g)
  }
  
}
