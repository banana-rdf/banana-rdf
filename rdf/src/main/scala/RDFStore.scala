package org.w3.rdf

trait RDFStore[Rdf <: RDF, Sparql <: SPARQL] {

  type Store

  def asGraph(store: Store): Rdf#Graph

  def addGraph(store: Store, graph: Rdf#Graph): Store

  def addNamedGraph(store: Store, uri: Rdf#IRI, graph: Rdf#Graph): Store

  def getNamedGraph(store: Store, uri: Rdf#IRI): Rdf#Graph

  def removeGraph(store: Store, uri: Rdf#IRI): Store

}
