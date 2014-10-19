package org.w3.banana.io

import org.w3.banana.{ RDF, RDFOps }

abstract class RdfXMLTestSuite[Rdf <: RDF](implicit ops: RDFOps[Rdf], reader: RDFReader[Rdf, RDFXML], writer: RDFWriter[Rdf, RDFXML])
    extends SerialisationTestSuite[Rdf, RDFXML] {

  def referenceGraphSerialisedForSyntax = """
  <rdf:RDF xmlns="http://purl.org/dc/elements/1.1/"
           xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <rdf:Description rdf:about="http://www.w3.org/2001/sw/RDFCore/ntriples/">
      <creator>Art Barstow</creator>
      <creator>Dave Beckett</creator>
      <publisher rdf:resource="http://www.w3.org/"/>
    </rdf:Description>
  </rdf:RDF>"""
}
