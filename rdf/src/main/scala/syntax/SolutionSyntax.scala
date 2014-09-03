package org.w3.banana.syntax

import org.w3.banana._

import scala.util.Try

trait SolutionSyntax[Rdf <: RDF] { self: SolutionSyntax[Rdf] =>

  implicit def solutionW(solution: Rdf#Solution) = new SolutionW[Rdf](solution)

}

class SolutionW[Rdf <: RDF](val solution: Rdf#Solution) extends AnyVal {

  def varnames()(implicit sparqlOps: SparqlOps[Rdf]): Set[String] =
    sparqlOps.varnames(solution)

  def getNode(v: String)(implicit sparqlOps: SparqlOps[Rdf]): Try[Rdf#Node] =
    sparqlOps.getNode(solution, v)

}
