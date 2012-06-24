package org.w3.banana

import scalaz.{Right3, Middle3, Left3, Either3}

/**
 * to execute SPARQL queries on an RDF graph
 */
trait RDFGraphQuery[Rdf <: RDF, Sparql <: SPARQL] {

  def executeSelect(graph: Rdf#Graph, query: Sparql#SelectQuery): Sparql#Solutions

  def executeConstruct(graph: Rdf#Graph, query: Sparql#ConstructQuery): Rdf#Graph

  def executeAsk(graph: Rdf#Graph, query: Sparql#AskQuery): Boolean

}

// TODO to be moved somewhere else please
case class OpenGraphQuery[Rdf <: RDF, Sparql <: SPARQL](graphQuery: RDFGraphQuery[Rdf,Sparql], ops: SPARQLOperations[Rdf,Sparql]) {

  import graphQuery._

  type Answer = Either3[Sparql#Solutions, Rdf#Graph, Boolean]

  /**
   * This takes a generic query, and returns whatever type of object that query returns
   * @param query
   * @return an Iterable[Sparql#Row] if the query was a select query,
   *         an Rdf#Graph if the query was a Construct query
   *         a boolean if the query was an ASK query
   */
  def executeQuery(graph: Rdf#Graph, query: Sparql#Query): Answer = ops.fold(query)(
    select => Left3(executeSelect(graph,select)),
    construct => Middle3(executeConstruct(graph,construct)),
    ask => Right3(executeAsk(graph,ask))
  )

}
