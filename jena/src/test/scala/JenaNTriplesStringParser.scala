package org.w3.rdf.jena

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

object JenaNTriplesStringParser
extends n3.Parser(
  JenaModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener](4)))

object JenaNTriplesSeqParser
extends n3.Parser(
  JenaModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener](4)))