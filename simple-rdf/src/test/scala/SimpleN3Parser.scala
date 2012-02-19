package org.w3.rdf.simple

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf.n3

object SimpleN3StringParser
extends n3.Parser(
  SimpleModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[n3.Listener](4)))

object SimpleN3SeqParser
extends n3.Parser(
  SimpleModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[n3.Listener](4)))