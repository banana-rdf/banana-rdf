package org.w3.banana

import scala.concurrent.Future

/**
 * A typeclass for SPARQL engines.
 *
 * The supported queries are: Select, Ask, and Construct. No support
 * for SPARQL Update here.
 */
trait SparqlEngine[Rdf <: RDF, A] {

  /** Executes a Select query. */
  def executeSelect(a: A, query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): Future[Rdf#Solutions]

  /** Executes a Construct query. */
  def executeConstruct(a: A, query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): Future[Rdf#Graph]

  /** Executes a Ask query. */
  def executeAsk(a: A, query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): Future[Boolean]

  val sparqlEngineSyntax = new syntax.SparqlEngineSyntax[Rdf, A]

}
