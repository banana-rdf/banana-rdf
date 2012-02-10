package org.w3.rdf.jena

import org.w3.rdf._
import nomo.{Accumulators, Errors, Monotypic, Parsers}

object JenaNTriplesParser extends NTriplesParser(JenaModule,
  Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Unit](4)))