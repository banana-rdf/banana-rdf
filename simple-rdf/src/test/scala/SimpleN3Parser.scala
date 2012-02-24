package org.w3.rdf.simple

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf.n3

object SimpleTurtleStringParser
extends n3.TurtleParser(
  SimpleRDFOperations,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[n3.Listener[SimpleRDF]](4)))

object SimpleTurtleSeqParser
extends n3.TurtleParser(
  SimpleRDFOperations,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[n3.Listener[SimpleRDF]](4)))