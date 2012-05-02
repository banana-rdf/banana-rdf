package org.w3.rdf

trait SPARQLQueryExecution[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelectQuery(graph: Rdf#Graph, query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(graph: Rdf#Graph, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAskQuery(graph: Rdf#Graph, query: Sparql#AskQuery): Boolean

}
