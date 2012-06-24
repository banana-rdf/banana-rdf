package org.w3.banana.sesame

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.banana._
import org.w3.banana.n3._

object NTriplesStringParser
extends NTriplesParser(
  Sesame.diesel,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener[Sesame]](4)))

object NTriplesSeqParser
extends NTriplesParser(
  Sesame.diesel,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener[Sesame]](4)))

object TurtleStringParser
  extends TurtleParser(
    Sesame.diesel,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[Sesame]](4)))

object TurtleSeqParser
  extends TurtleParser(
    Sesame.diesel,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[Sesame]](4)))
