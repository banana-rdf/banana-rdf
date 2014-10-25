package org.w3.banana.io

import org.w3.banana.{ RDFOps, RDF }
import scala.util.Try

abstract class TurtleTestSuite[Rdf <: RDF](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, Try, Turtle],
  writer: RDFWriter[Rdf, Turtle]
) extends SerialisationTestSuite[Rdf, Turtle] {

  val referenceGraphSerialisedForSyntax = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """

}
