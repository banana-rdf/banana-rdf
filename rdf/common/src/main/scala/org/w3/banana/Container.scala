package org.w3.banana

trait Container[Rdf <: RDF, T] {

  def uri: Rdf#URI

}
