package org.w3.banana.syntax

import org.w3.banana._

trait RDFSyntax[Rdf <: RDF]
  extends GraphSyntax[Rdf]
  with TripleMatchSyntax[Rdf]
  with TripleSyntax[Rdf]
  with NodeMatchSyntax[Rdf]
  with NodeSyntax[Rdf]
  with URISyntax[Rdf]
  with LiteralSyntax[Rdf]
  with StringSyntax[Rdf]
  with AnySyntax[Rdf]
  with SparqlSolutionSyntax[Rdf]
  with SparqlSolutionsSyntax[Rdf]
