package org.w3.banana.syntax

import org.w3.banana._

trait SolutionsSyntax[Rdf <: RDF] { self: SolutionsSyntax[Rdf] =>

  implicit def solutionsW(solutions: Rdf#Solutions) = new SolutionsW[Rdf](solutions)

}

class SolutionsW[Rdf <: RDF](val solutions: Rdf#Solutions) extends AnyVal {

  def iterator()(implicit sparqlOps: SparqlOps[Rdf]): Iterator[Rdf#Solution] =
    sparqlOps.solutionIterator(solutions)

}
