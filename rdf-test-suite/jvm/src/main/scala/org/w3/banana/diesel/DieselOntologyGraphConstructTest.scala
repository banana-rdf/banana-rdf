package org.w3.banana.diesel

import org.w3.banana._
import org.w3.banana.syntax._
import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

import scalaz.Scalaz._

abstract class DieselOntologyGraphConstructTest[Rdf <: RDF]()(
    implicit ops: RDFOps[Rdf],
    turtleReader: RDFReader[Rdf, Turtle],
    turtleWriter: RDFWriter[Rdf, Turtle],
    rdfXMLReader: RDFReader[Rdf, RDFXML],
    rdfXMLWriter: RDFWriter[Rdf, RDFXML])
    extends WordSpec with Matchers {

  import ops._

  val rdfs = RDFSPrefix[Rdf]
  val owl = OWLPrefix[Rdf]

  "Diesel supports creating basic ontologies" in {

    val g: PointedGraph[Rdf] = (
      URI("http://example.org/families").a(owl.Ontology)
    )

    val expectedGraph =
      Graph(
        Triple(URI("http://example.org/families"), rdf.`type`, owl.Ontology))

    assert(g.graph isIsomorphicWith expectedGraph)

  }

  "Diesel supports constructing ontologies like the examples in the OWL2 RDF-based semantics" when {

    val ex = "http://example.org/"
    val ex1 = ex + "o1"
    val ex2 = ex + "o2"
    def bURI(fragment: String) = URI(ex + fragment)
    
    val o1 = Seq(
      bURI("o1").a(owl.Ontology),
      bURI("c1").a(owl.Class),
      bURI("c2").a(owl.Class),
      bURI("c1") -- rdfs.subClassOf ->- bURI("c2")
    )
    val g1 = union( o1 map (_.graph) )
    
    val o2 = Seq(
      bURI("o2").a(owl.Ontology),
      bURI("c1").a(owl.Class),
      bURI("c2").a(owl.Class),
      bURI("c3").a(owl.Class),
      bURI("c1") -- rdfs.subClassOf ->- (
          bnode().a(owl.Class)
          -- owl.unionOf ->- List( bURI("c2"), bURI("c3"))
          ),
      bURI("c3") -- rdfs.label ->- "c3"
    )
    val g2 = union( o2 map (_.graph) )
    
    val t1 =  Triple(URI("http://example.org/o1"), rdf.`type`, owl.Ontology)
    val t2 =  Triple(URI("http://example.org/o2"), rdf.`type`, owl.Ontology)
        	   
    "tbox should have the correct ontology IRI" in {
      g1 contains(t1) should be(true)
      g2 contains(t2) should be(true)
    }
    
    val tboxTurtleRep1 = turtleWriter.asString(g1, ex1)
    assert(tboxTurtleRep1.isSuccess)
    println("------------------------")
    println(tboxTurtleRep1.get)
    println("------------------------")
    val tboxTurtleGraph1a = Await.result(turtleReader.read(tboxTurtleRep1.get, ex1 ), Duration(1, MINUTES))
    val tboxTurtleGraph1b = Await.result(turtleReader.read(tboxTurtleRep1.get, ex2 ), Duration(1, MINUTES))
    
    val tboxTurtleRep2 = turtleWriter.asString(g2, ex1)
    assert(tboxTurtleRep2.isSuccess)
    println("------------------------")
    println(tboxTurtleRep2.get)
    println("------------------------")
    val tboxTurtleGraph2a = Await.result(turtleReader.read(tboxTurtleRep2.get, ex2 ), Duration(1, MINUTES))
    val tboxTurtleGraph2b = Await.result(turtleReader.read(tboxTurtleRep2.get, ex1 ), Duration(1, MINUTES))
    
    "serializing to turtle & reading the graph back with the same base shouldn't change the ontology IRI" in {
      tboxTurtleGraph1a contains(t1) should be(true)
      tboxTurtleGraph1a contains(t2) should be(false)
      
      tboxTurtleGraph2a contains(t1) should be(false)
      tboxTurtleGraph2a contains(t2) should be(true)
    }
    
    "serializing to turtle & reading the graph back with a different base shouldn't change the ontology IRI" in {
      tboxTurtleGraph1b contains(t1) should be(true)
      tboxTurtleGraph1b contains(t2) should be(false)
      
      tboxTurtleGraph2b contains(t1) should be(false)
      tboxTurtleGraph2b contains(t2) should be(true)
    }
    
    val tboxRdfXMLRep1 = rdfXMLWriter.asString(g1, ex1)
    assert(tboxRdfXMLRep1.isSuccess)
    println("------------------------")
    println(tboxRdfXMLRep1.get)
    println("------------------------")
    val tboxRdfXMLGraph1a = Await.result(rdfXMLReader.read(tboxRdfXMLRep1.get, ex1), Duration(1, MINUTES))
    val tboxRdfXMLGraph1b = Await.result(rdfXMLReader.read(tboxRdfXMLRep1.get, ex2), Duration(1, MINUTES))
    
    val tboxRdfXMLRep2 = rdfXMLWriter.asString(g2, ex2)
    assert(tboxRdfXMLRep2.isSuccess)
    println("------------------------")
    println(tboxRdfXMLRep2.get)
    println("------------------------")
    val tboxRdfXMLGraph2a = Await.result(rdfXMLReader.read(tboxRdfXMLRep2.get, ex2), Duration(1, MINUTES))
    val tboxRdfXMLGraph2b = Await.result(rdfXMLReader.read(tboxRdfXMLRep2.get, ex1), Duration(1, MINUTES))
    
   "serializing to RDF/XML & reading the graph back with the same base shouldn't change the ontology IRI" in {
      tboxRdfXMLGraph1a contains(t1) should be(true)
      tboxRdfXMLGraph1a contains(t2) should be(false)
      
      tboxRdfXMLGraph2a contains(t1) should be(false)
      tboxRdfXMLGraph2a contains(t2) should be(true)
    }
    "serializing to RDF/XML & reading the graph back with a different base shouldn't change the ontology IRI" in {
      tboxRdfXMLGraph1b contains(t1) should be(true)
      tboxRdfXMLGraph1b contains(t2) should be(false)
      
      tboxRdfXMLGraph2b contains(t1) should be(false)
      tboxRdfXMLGraph2b contains(t2) should be(true)
    }
    
    "reading the serializations to Turtle & to RDF/XML with the same base should produce isomorphic graphs" in {
      tboxTurtleGraph1a isIsomorphicWith tboxRdfXMLGraph1a should be(true)
      tboxTurtleGraph2a isIsomorphicWith tboxRdfXMLGraph2a should be(true)
    }
    
    "reading the serializations to Turtle & to RDF/XML with the different Turtle base should produce isomorphic graphs" in {
      tboxTurtleGraph1b isIsomorphicWith tboxRdfXMLGraph1a should be(true)
      tboxTurtleGraph2b isIsomorphicWith tboxRdfXMLGraph2a should be(true)
    }
    
    "reading the serializations to Turtle & to RDF/XML with the different RDF/XML base should produce isomorphic graphs" in {
      tboxTurtleGraph1a isIsomorphicWith tboxRdfXMLGraph1b should be(true)
      tboxTurtleGraph2a isIsomorphicWith tboxRdfXMLGraph2b should be(true)
    }
    
    "reading the serializations to Turtle & to RDF/XML with different bases should produce isomorphic graphs" in {
      tboxTurtleGraph1b isIsomorphicWith tboxRdfXMLGraph1b should be(true)
      tboxTurtleGraph2b isIsomorphicWith tboxRdfXMLGraph2b should be(true)
    }
  }

}
