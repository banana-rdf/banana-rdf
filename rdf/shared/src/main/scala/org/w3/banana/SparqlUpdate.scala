package org.w3.banana

trait SparqlUpdate[Rdf <: RDF, M[_], A] {

  def executeUpdate(a: A, query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node]): M[Unit]

  val sparqlUpdateSyntax = new syntax.SparqlUpdateSyntax[Rdf, M, A]

}
