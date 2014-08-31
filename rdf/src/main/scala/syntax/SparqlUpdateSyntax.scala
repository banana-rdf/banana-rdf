package org.w3.banana.syntax

import org.w3.banana._

final class SparqlUpdateSyntax[Rdf <: RDF, A] {

  implicit def sparqlUpdateSyntaxW(a: A) = new SparqlUpdateSyntaxW[Rdf, A](a)

}

final class SparqlUpdateSyntaxW[Rdf <: RDF, A](val a: A) extends AnyVal {

  def executeUpdate(query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node] = Map.empty)(implicit sparqlUpdate: SparqlUpdate[Rdf, A]) =
    sparqlUpdate.executeUpdate(a, query, bindings)

}
