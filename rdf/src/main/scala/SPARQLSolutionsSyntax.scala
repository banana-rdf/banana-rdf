package org.w3.banana

import scalaz.Validation

object SPARQLSolutionsSyntax {

  def apply[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SPARQLOperations[Rdf]) =
    new SPARQLSolutionsSyntax[Rdf](solutions)(sparqlOps)

}

class SPARQLSolutionsSyntax[Rdf <: RDF](solutions: Rdf#Solutions)(implicit sparqlOps: SPARQLOperations[Rdf]) {

  def toIterable = sparqlOps.solutionIterator(solutions)

}
