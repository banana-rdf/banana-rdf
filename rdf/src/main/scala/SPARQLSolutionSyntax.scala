package org.w3.banana

import scalaz.Validation

object SPARQLSolutionSyntax {

  def apply[Rdf <: RDF, Sparql <: SPARQL](solution: Sparql#Solution)(implicit sparqlOps: SPARQLOperations[Rdf, Sparql]) = new SPARQLSolutionSyntax[Rdf, Sparql](solution)(sparqlOps)

}

class SPARQLSolutionSyntax[Rdf <: RDF, Sparql <: SPARQL](solution: Sparql#Solution)(implicit sparqlOps: SPARQLOperations[Rdf, Sparql]) {

  def apply(v: String): Validation[BananaException, Rdf#Node] =
    sparqlOps.getNode(solution, v)

  def vars: Set[String] = sparqlOps.varnames(solution)

}
