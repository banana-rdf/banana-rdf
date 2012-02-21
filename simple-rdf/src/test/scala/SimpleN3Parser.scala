package org.w3.rdf.simple

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf.n3

object SimpleTurtleStringParser
extends n3.TurtleParser(
  SimpleModule,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[n3.Listener](4)))

object SimpleTurtleSeqParser
extends n3.TurtleParser(
  SimpleModule,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[n3.Listener](4)))