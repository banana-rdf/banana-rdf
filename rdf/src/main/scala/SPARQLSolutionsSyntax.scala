package org.w3.banana


object SPARQLSolutionsSyntax {

  def apply[Rdf <: RDF, Sparql <: SPARQL](solutions: Sparql#Solutions)(implicit sparqlOps: SPARQLOperations[Rdf, Sparql]) = new SPARQLSolutionsSyntax[Rdf, Sparql](solutions)(sparqlOps)

}

class SPARQLSolutionsSyntax[Rdf <: RDF, Sparql <: SPARQL](solutions: Sparql#Solutions)(implicit sparqlOps: SPARQLOperations[Rdf, Sparql]) {

  def toIterable = sparqlOps.solutionIterator(solutions)

}
