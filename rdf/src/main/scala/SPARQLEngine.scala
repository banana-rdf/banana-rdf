package org.w3.banana

import util._
import scalaz._
import Id._

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

}
