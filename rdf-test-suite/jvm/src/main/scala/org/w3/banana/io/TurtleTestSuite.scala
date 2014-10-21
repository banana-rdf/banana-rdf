package org.w3.banana.io

import org.w3.banana.{ RDFOps, RDF }

abstract class TurtleTestSuite[Rdf <: RDF](implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, Turtle], writer: RDFWriter[Rdf, Turtle])
    extends SerialisationTestSuite[Rdf, Turtle] {

  def referenceGraphSerialisedForSyntax = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """
}
