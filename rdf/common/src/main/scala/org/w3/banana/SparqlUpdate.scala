package org.w3.banana

trait SparqlUpdate[Rdf <: RDF, M[_], A] {

  /**
   * run a Sparql Update query against a given object A.
   * @param a
   * @param query
   * @param bindings
   * @return the changed A
   */
  def executeUpdate(a: A, query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node]): M[A]

  val sparqlUpdateSyntax = new syntax.SparqlUpdateSyntax[Rdf, M, A]

}
