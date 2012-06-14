package org.w3.banana

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelect(query: Sparql#SelectQuery): Iterable[Sparql#Row]

  def executeConstruct(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(query: Sparql#AskQuery): Boolean

  /**
   * This takes a generic query, and returns whatever a query returns
   * @param query
   * @return an Iterable[Sparql#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
  def executeQuery(query: Sparql#Query): Any

}
