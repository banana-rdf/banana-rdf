package org.w3.rdf.sesame

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

object NTriplesStringParser
extends n3.NTriplesParser(
  SesameModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener[SesameModule.type ]](4)))

object NTriplesSeqParser
extends n3.NTriplesParser(
  SesameModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener[SesameModule.type ]](4)))