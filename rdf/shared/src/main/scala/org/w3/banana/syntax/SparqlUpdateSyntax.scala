package org.w3.banana.syntax

import org.w3.banana._

final class SparqlUpdateSyntax[Rdf <: RDF, M[_], A] {

  implicit def sparqlUpdateSyntaxW(a: A) = new SparqlUpdateSyntaxW[Rdf, M, A](a)

}

final class SparqlUpdateSyntaxW[Rdf <: RDF, M[_], A](val a: A) extends AnyVal {

  def executeUpdate(query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node] = Map.empty)(implicit sparqlUpdate: SparqlUpdate[Rdf, M, A]) =
    sparqlUpdate.executeUpdate(a, query, bindings)

}
