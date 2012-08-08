package org.w3.banana

import util._
import scalaz._
import scalaz.Id
import scalaz.Right3
import scalaz.Middle3
import scalaz.Left3

trait SPARQLEngine[Rdf <: RDF] extends MSPARQLEngine[Rdf, Id]

trait AsyncSPARQLEngine[Rdf <: RDF] extends MSPARQLEngine[Rdf, BananaFuture]

/**
 * to execute SPARQL queries
 */
trait MSPARQLEngine[Rdf <: RDF, M[_]] {

  def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions]

  def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph]

  def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean]

  def executeSelect(query: Rdf#SelectQuery): M[Rdf#Solutions] = executeSelect(query, Map.empty)

  def executeConstruct(query: Rdf#ConstructQuery): M[Rdf#Graph] = executeConstruct(query, Map.empty)

  def executeAsk(query: Rdf#AskQuery): M[Boolean] = executeAsk(query, Map.empty)

//
// type Answer = Either3[Rdf#Solutions, Rdf#Graph, Boolean]
//
// It would be good to be able to add the following here:
//
//  /**
//   * This takes a generic query, and returns whatever type of object that query returns
//   * @param query
//   * @return an Iterable[Rdf#Row] if the query was a select query,
//   *         an Rdf#Graph if the query was a Construct query
//   *         a boolean if the query was an ASK query
//   */
//  def executeQuery(query: Rdf#Query): M[ = ops.fold(query)(
//    select => Left3(executeSelect(graph, select)),
//    construct => Middle3(executeConstruct(graph, construct)),
//    ask => Right3(executeAsk(graph, ask))
//  )


}
