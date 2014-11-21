package org.w3.banana

/**
 * A typeclass for SPARQL engines.
 *
 * The supported queries are: Select, Ask, and Construct. No support
 * for SPARQL Update here.
 */
trait SparqlEngine[Rdf <: RDF, M[_], A] {

  /** Executes a Select query. */
  def executeSelect(a: A, query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Solutions]

  /** Executes a Construct query. */
  def executeConstruct(a: A, query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node]): M[Rdf#Graph]

  /** Executes a Ask query. */
  def executeAsk(a: A, query: Rdf#AskQuery, bindings: Map[String, Rdf#Node]): M[Boolean]

  val sparqlEngineSyntax = new syntax.SparqlEngineSyntax[Rdf, M, A]

}
