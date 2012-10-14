package org.w3.banana.syntax

import org.w3.banana._

class SparqlSolutionsSyntax[Rdf <: RDF](val solutions: Rdf#Solutions) extends AnyVal {

  def toIterable(implicit sparqlOps: SparqlOps[Rdf]) = sparqlOps.solutionIterator(solutions)

}
