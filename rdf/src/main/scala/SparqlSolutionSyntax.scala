package org.w3.banana

import scala.util._

object SparqlSolutionSyntax {

  def apply[Rdf <: RDF](solution: Rdf#Solution)(implicit sparqlOps: SparqlOps[Rdf]) = new SparqlSolutionSyntax[Rdf](solution)(sparqlOps)

}

class SparqlSolutionSyntax[Rdf <: RDF](solution: Rdf#Solution)(implicit sparqlOps: SparqlOps[Rdf]) {

  def apply(v: String): Try[Rdf#Node] =
    sparqlOps.getNode(solution, v)

  def vars: Set[String] = sparqlOps.varnames(solution)

}
