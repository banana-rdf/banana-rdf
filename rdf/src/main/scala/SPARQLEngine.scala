package org.w3.banana

import scalaz.{Left3, Right3, Middle3, Either3}

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL]  {

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
  def executeQuery(query: Sparql#Query): Either3[Iterable[Sparql#Row],Rdf#Graph,Boolean]


}

trait SPARQLEngineSyntax[Rdf <: RDF, Sparql <: SPARQL] {
  this: SPARQLEngine[Rdf,Sparql] =>

   def ops: SPARQLOperations[Rdf,Sparql]

   def executeQuery(query: Sparql#Query) = ops.fold(query)(
    select => Left3(executeSelect(select)),
    construct => Middle3(executeConstruct(construct)),
    ask => Right3(executeAsk(ask))
  )

}
