package org.w3.banana.syntax

import org.w3.banana._

final class SparqlEngineSyntax[Rdf <: RDF, M[_], A] {

  implicit def sparqlEngineW(a: A) = new SparqlEngineW[Rdf, M, A](a)

}

final class SparqlEngineW[Rdf <: RDF, M[_], A](val a: A) extends AnyVal {

  def executeSelect(query: Rdf#SelectQuery, bindings: Map[String, Rdf#Node] = Map.empty)(implicit sparqlEngine: SparqlEngine[Rdf, M, A]) =
    sparqlEngine.executeSelect(a, query, bindings)

  def executeConstruct(query: Rdf#ConstructQuery, bindings: Map[String, Rdf#Node] = Map.empty)(implicit sparqlEngine: SparqlEngine[Rdf, M, A]) =
    sparqlEngine.executeConstruct(a, query, bindings)

  def executeAsk(query: Rdf#AskQuery, bindings: Map[String, Rdf#Node] = Map.empty)(implicit sparqlEngine: SparqlEngine[Rdf, M, A]) =
    sparqlEngine.executeAsk(a, query, bindings)

}
