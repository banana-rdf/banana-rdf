package org.w3.rdf

/**
 * to manipulate named graph within a Store
 */
trait RDFStore[Rdf <: RDF] {

  def addNamedGraph(store: Rdf#Store, uri: Rdf#IRI, graph: Rdf#Graph): Rdf#Store

  def appendToNamedGraph(store: Rdf#Store, uri: Rdf#IRI, graph: Rdf#Graph): Rdf#Store

  def getNamedGraph(store: Rdf#Store, uri: Rdf#IRI): Rdf#Graph

  def removeGraph(store: Rdf#Store, uri: Rdf#IRI): Rdf#Store

}
