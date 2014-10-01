package org.w3.banana.syntax

import org.w3.banana._

trait SparqlSyntax[Rdf <: RDF]
  extends SparqlQuerySyntax[Rdf]
  with SolutionsSyntax[Rdf]
  with SolutionSyntax[Rdf]
