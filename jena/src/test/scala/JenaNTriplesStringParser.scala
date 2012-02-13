package org.w3.rdf.test

import org.w3.rdf._
import org.w3.rdf.jena.JenaModule
import nomo.{Accumulators, Errors, Monotypic, Parsers}

object JenaNTriplesStringParser extends NTriplesParser(JenaModule,
  Parsers(Monotypic.String, Errors.tree[Char], Accumulators.position[Unit](4)))

object JenaNTriplesSeqParser extends NTriplesParser(JenaModule,
  Parsers(Monotypic.Seq[Char],Errors.tree[Char], Accumulators.position[Unit](4)))