package org.w3.banana.io

import org.w3.banana.{ RDFOps, RDF }
import scalaz._

abstract class TurtleTestSuite[Rdf <: RDF, M[+_] : Monad : Comonad](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, Turtle],
  writer: RDFWriter[Rdf, M, Turtle]
) extends SerialisationTestSuite[Rdf, M, Turtle, Turtle]("Turtle", "ttl") {

  val referenceGraphSerialisedForSyntax = """
<http://www.w3.org/2001/sw/RDFCore/ntriples/> <http://purl.org/dc/elements/1.1/creator> "Dave Beckett", "Art Barstow" ;
                                              <http://purl.org/dc/elements/1.1/publisher> <http://www.w3.org/> .
  """

}
