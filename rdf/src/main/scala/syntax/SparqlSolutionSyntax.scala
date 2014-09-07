package org.w3.banana.syntax

import org.w3.banana._

import scala.util._

trait SparqlSolutionSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def sparqlSolutionSyntax(solution: Rdf#Solution) = new SparqlSolutionSyntaxW[Rdf](solution)

}

class SparqlSolutionSyntaxW[Rdf <: RDF](val solution: Rdf#Solution) extends AnyVal {

  def apply(v: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#Node] =
    sparqlOps.getNode(solution, v)

  def vars(implicit sparqlOps: SparqlOps[Rdf]): Set[String] = sparqlOps.varnames(solution)

}
