package org.w3.banana

import org.scalatest._
import org.scalatest.matchers.MustMatchers
import java.io._
import scalax.io._
import org.scalatest.EitherValues._

import scalaz.Validation
import scalaz.Validation._

abstract class TurtleTestSuite[Rdf <: RDF]()(implicit val diesel: Diesel[Rdf])
    extends WordSpec with MustMatchers {

  val reader: RDFReader[Rdf, Turtle]
  val writer: RDFWriter[Rdf, Turtle]
  import diesel._
  import ops._

  import org.scalatest.matchers.{ BeMatcher, MatchResult }

  def graphBuilder(prefix: Prefix[Rdf]) = {
    val ntriples = prefix("ntriples/")
    val creator = URI("http://purl.org/dc/elements/1.1/creator")
    val publisher = URI("http://purl.org/dc/elements/1.1/publisher")
    val dave = TypedLiteral("Dave Beckett")
    val art = TypedLiteral("Art Barstow")
    val w3org = URI("http://www.w3.org/")
    Graph(
      Triple(ntriples, creator, dave),
      Triple(ntriples, creator, art),
      Triple(ntriples, publisher, w3org)
    )
  }

  val rdfCore = "http://www.w3.org/2001/sw/RDFCore/"
  val rdfCorePrefix = Prefix("rdf", rdfCore)
  val referenceGraph = graphBuilder(rdfCorePrefix)

  // TODO: there is a bug in Sesame with hash uris as prefix
  val foo = "http://example.com/foo/"
  val fooPrefix = Prefix("foo", foo)
  val fooGraph = graphBuilder(fooPrefix)

  "read TURTLE version of timbl's card" in {
    val file = new File("rdf-test-suite/src/main/resources/card.ttl")
    val resource = Resource.fromFile(file)
    val graph = reader.read(resource, file.toURI.toString).fold(t => throw t, g => g)
    //    graph.fold( _.printStackTrace, r => println(r.size))
    graph.toIterable.size must equal(77)
  }

  "read simple TURTLE String" in {
    val turtleString = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
 """
    import scalax.io.JavaConverters._
    val graph = reader.read(turtleString, rdfCore).fold(t => throw t, g => g)
    assert(referenceGraph isIsomorphicWith graph)

  }

  "write simple graph as TURTLE string" in {
    val turtleString = writer.asString(referenceGraph, "http://www.w3.org/2001/sw/RDFCore/").fold(t => throw t, s => s)
    turtleString must not be ('empty)
  }

  "works with relative uris" taggedAs (JenaWIP) in {
    val bar = for {
      turtleString <- writer.asString(referenceGraph, rdfCore)
      computedFooGraph <- reader.read(turtleString, foo)
    } yield computedFooGraph
    val g: Rdf#Graph = bar.fold(t => throw t, g => g)
    assert(fooGraph isIsomorphicWith g)
  }

}
