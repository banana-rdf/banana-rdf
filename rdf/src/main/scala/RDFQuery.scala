package org.w3.banana

/**
 * to execute SPARQL queries against a Dataset
 */
trait RDFQuery[Rdf <: RDF, Sparql <: SPARQL] {

  type Dataset

  def executeSelectQuery(dataset: Dataset, query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def getNode(row: Sparql#Row, v: String): Rdf#Node

  def executeConstructQuery(dataset: Dataset, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAskQuery(dataset: Dataset, query: Sparql#AskQuery): Boolean

}

trait RDFGraphQuery[Rdf <: RDF, Sparql <: SPARQL] extends RDFQuery[Rdf, Sparql] {

  type Dataset = Rdf#Graph

}

trait RDFStoreQuery[Rdf <: RDF, Sparql <: SPARQL] extends RDFQuery[Rdf, Sparql] {

  type Dataset = Rdf#Store

}
