package org.w3.banana

/**
 * to execute SPARQL queries against a Dataset
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelect(query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def executeConstruct(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(query: Sparql#AskQuery): Boolean

}
