package org.w3.rdf.test

import org.junit.Test
import org.junit.Assert._
import org.w3.rdf.{GraphIsomorphism, NTriplesParser, Module}

// would be happy to use
// NTriplesParserTest[M <: Model](m: M, parser: NTriplesParser[m.type], isomorphism: GraphIsomorphism[m.type])
// but the compiler complains, saying it does not know m
abstract class NTriplesParserTest[M <: Module](val m: M) {

  val parser: NTriplesParser[m.type]
  val isomorphism: GraphIsomorphism[m.type]
  
  import m._
  import isomorphism._
  
  @Test()
  def read_simple_n3(): Unit = {
    val n3 =
"""<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett" .
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Art Barstow" .
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> ."""
    
    implicit val U: Unit = ()
    val parsedGraph = Graph(parser.ntriples(n3).get)
    
    val ntriples = IRI("http://www.w3.org/2001/sw/RDFCore/ntriples/")
    val creator = IRI("http://purl.org/dc/elements/1.1/creator")
    val publisher = IRI("http://purl.org/dc/elements/1.1/publisher")
    val dave = Literal("Dave Beckett",xsdStringIRI)
    val art = Literal("Art Barstow")
    val w3org = IRI("http://www.w3.org/")
    
    val expected = 
      Graph(
        Triple(ntriples, creator, dave),
        Triple(ntriples, creator, art),
        Triple(ntriples, publisher, w3org)
      )
    
    assertTrue(isIsomorphicWith(expected, parsedGraph))
    
  }
}