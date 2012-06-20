package org.w3.banana

import scalaz.{Right3, Middle3, Left3, Either3}

/**
 * to execute SPARQL queries on an RDF graph
 */
trait RDFGraphQuery[Rdf <: RDF, Sparql <: SPARQL] extends SPARQLEngine[Rdf,Sparql]  {
  def graph: Rdf#Graph
}

trait OpenGraphQuery[Rdf <: RDF, Sparql <: SPARQL] {
  self: RDFGraphQuery[Rdf,Sparql] =>

  def ops: SPARQLOperations[Rdf,Sparql]

  type Answer = Either3[Sparql#Solutions, Rdf#Graph, Boolean]

  /**
   * This takes a generic query, and returns whatever type of object that query returns
   * @param query
   * @return an Iterable[Sparql#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
  def executeQuery(query: Sparql#Query): Answer = ops.fold(query)(
    select => Left3(executeSelect(select)),
    construct => Middle3(executeConstruct(construct)),
    ask => Right3(executeAsk(ask))
  )

}
