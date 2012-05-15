package org.w3.banana

/**
 * to execute SPARQL queries against a Dataset
 */
trait RDFQuery[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelectQuery(query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAskQuery(query: Sparql#AskQuery): Boolean

}
