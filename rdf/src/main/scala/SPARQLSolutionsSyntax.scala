package org.w3.banana

import scalaz.Validation

object SPARQLSolutionsSyntax {

  def apply[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SPARQLOps[Rdf]) =
    new SPARQLSolutionsSyntax[Rdf](solutions)(sparqlOps)

}

class SPARQLSolutionsSyntax[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SPARQLOps[Rdf]) {

  def toIterable = sparqlOps.solutionIterator(solutions)

}
