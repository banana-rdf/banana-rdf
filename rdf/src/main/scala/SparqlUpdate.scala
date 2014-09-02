package org.w3.banana

import scala.concurrent.Future

trait SparqlUpdate[Rdf <: RDF, A] {

  def executeUpdate(a: A, query: Rdf#UpdateQuery, bindings: Map[String, Rdf#Node]): Future[Unit]

  val sparqlUpdateSyntax = new syntax.SparqlUpdateSyntax[Rdf, A]

}
