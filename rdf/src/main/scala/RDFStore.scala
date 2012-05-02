package org.w3.rdf

trait RDFStore[Rdf <: RDF, Sparql <: SPARQL] {

  type Store

  def addGraph(store: Store, graph: Rdf#Graph): Store

  def addNamedGraph(store: Store, uri: Rdf#IRI, graph: Rdf#Graph): Store

  def getNamedGraph(store: Store, uri: Rdf#IRI): Rdf#Graph

  def executeSelectQuery(store: Store, query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(store: Store, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAskQuery(graph: Store, query: Sparql#AskQuery): Boolean


}
