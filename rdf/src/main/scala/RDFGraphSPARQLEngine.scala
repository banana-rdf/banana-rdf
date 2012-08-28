package org.w3.banana

trait RDFGraphQuery[Rdf <: RDF] {

  def makeSPARQLEngine(graph: Rdf#Graph): SPARQLEngine[Rdf]

}
