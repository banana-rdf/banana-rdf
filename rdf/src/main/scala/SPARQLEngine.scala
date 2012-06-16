package org.w3.banana

import scalaz.{Left3, Right3, Middle3, Either3}

/**
 * to execute SPARQL queries
 */
trait SPARQLEngine[Rdf <: RDF, Sparql <: SPARQL]  {

  def executeSelect(query: Sparql#SelectQuery): Iterable[PartialFunction[String, Rdf#Node]]

  def executeConstruct(query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(query: Sparql#AskQuery): Boolean

}

trait OpenSPARQLEngine[Rdf <: RDF, Sparql <: SPARQL] {
  this: SPARQLEngine[Rdf,Sparql] =>

   def ops: SPARQLOperations[Rdf,Sparql]

  /**
   * This takes a generic query, and returns whatever type of object that query returns
   * @param query
   * @return an Iterable[Sparql#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
   def executeQuery(query: Sparql#Query) = ops.fold(query)(
    select => Left3(executeSelect(select)),
    construct => Middle3(executeConstruct(construct)),
    ask => Right3(executeAsk(ask))
  )

}
