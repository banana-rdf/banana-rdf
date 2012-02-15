package org.w3.rdf

import org.w3.rdf.jena.JenaModule
import nomo.{Accumulators, Errors, Monotypic, Parsers}

object JenaNTriplesStringParser
extends NTriplesParser(
  JenaModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener](4)))

object JenaNTriplesSeqParser
extends NTriplesParser(
  JenaModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener](4)))