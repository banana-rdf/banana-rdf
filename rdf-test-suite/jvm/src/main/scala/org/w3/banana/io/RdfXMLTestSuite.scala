package org.w3.banana.io

import org.w3.banana.{ RDF, RDFOps }
import scalaz._

abstract class RdfXMLTestSuite[Rdf <: RDF, M[+_] : Monad : Comonad](implicit
  ops: RDFOps[Rdf],
  reader: RDFReader[Rdf, M, RDFXML],
  writer: RDFWriter[Rdf, M, RDFXML]
) extends SerialisationTestSuite[Rdf, M, RDFXML, RDFXML]("RDF/XML", "rdf") {

  val referenceGraphSerialisedForSyntax = """
  <rdf:RDF xmlns="http://purl.org/dc/elements/1.1/"
           xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <rdf:Description rdf:about="http://www.w3.org/2001/sw/RDFCore/ntriples/">
      <creator>Art Barstow</creator>
      <creator>Dave Beckett</creator>
      <publisher rdf:resource="http://www.w3.org/"/>
    </rdf:Description>
  </rdf:RDF>"""

}
