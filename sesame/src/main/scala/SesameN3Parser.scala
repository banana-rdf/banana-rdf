package org.w3.rdf.sesame

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

object NTriplesStringParser
extends NTriplesParser(
  SesameOperations,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener[Sesame]](4)))

object NTriplesSeqParser
extends NTriplesParser(
  SesameOperations,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener[Sesame]](4)))

object TurtleStringParser
  extends TurtleParser(
    SesameOperations,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[Sesame]](4)))

object TurtleSeqParser
  extends TurtleParser(
    SesameOperations,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[Sesame]](4)))