package org.w3.rdf

/**
 * to execute SPARQL queries against a Dataset
 */
trait SPARQLQueryExecution[Rdf <: RDF, Sparql <: SPARQL] {

  type Dataset

  def executeSelectQuery(dataset: Dataset, query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(dataset: Dataset, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAskQuery(dataset: Dataset, query: Sparql#AskQuery): Boolean

}

trait SPARQLGraphQueryExecution[Rdf <: RDF, Sparql <: SPARQL] extends SPARQLQueryExecution[Rdf, Sparql] {

  type Dataset = Rdf#Graph

}
