package org.w3.banana

import scalaz.Validation

object SPARQLSolutionSyntax {

  def apply[Rdf <: RDF](solution: Rdf#Solution)(implicit sparqlOps: SPARQLOps[Rdf]) = new SPARQLSolutionSyntax[Rdf](solution)(sparqlOps)

}

class SPARQLSolutionSyntax[Rdf <: RDF](solution: Rdf#Solution)(implicit sparqlOps: SPARQLOps[Rdf]) {

  def apply(v: String): BananaValidation[Rdf#Node] =
    sparqlOps.getNode(solution, v)

  def vars: Set[String] = sparqlOps.varnames(solution)

}
