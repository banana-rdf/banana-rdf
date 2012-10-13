package org.w3.banana

object SparqlSolutionsSyntax {

  def apply[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SparqlOps[Rdf]) =
    new SparqlSolutionsSyntax[Rdf](solutions)(sparqlOps)

}

class SparqlSolutionsSyntax[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SparqlOps[Rdf]) {

  def toIterable = sparqlOps.solutionIterator(solutions)

}
