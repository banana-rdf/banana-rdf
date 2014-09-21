package org.w3.banana.syntax

import org.w3.banana._

trait SparqlSolutionsSyntax[Rdf <: RDF] { self: RDFSyntax[Rdf] =>

  implicit def sparqlSolutionsSyntax(solutions: Rdf#Solutions) = new SparqlSolutionsSyntaxW[Rdf](solutions)

}

class SparqlSolutionsSyntaxW[Rdf <: RDF](val solutions: Rdf#Solutions) extends AnyVal {

  def toIterable(implicit sparqlOps: SparqlOps[Rdf]) = sparqlOps.solutionIterator(solutions)

}
