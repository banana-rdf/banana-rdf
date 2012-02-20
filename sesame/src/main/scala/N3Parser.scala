package org.w3.rdf.sesame

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

object N3StringParser
extends n3.Parser(
  SesameModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener](4)))

object N3SeqParser
extends n3.Parser(
  SesameModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener](4)))