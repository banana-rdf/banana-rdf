package org.w3.banana.jena

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.banana._
import org.w3.banana.n3._

object JenaN3Parser {

  implicit val nTriplesStringParser = JenaNTriplesStringParser

  implicit val nTriplesSeqParser = JenaNTriplesSeqParser

  implicit val turtleStringParser = JenaTurtleStringParser

  implicit val turtleSeqParser = JenaTurtleSeqParser

}

object JenaNTriplesStringParser
extends n3.NTriplesParser(
  Jena.diesel,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener[Jena]](4)))

object JenaNTriplesSeqParser
extends n3.NTriplesParser(
  Jena.diesel,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener[Jena]](4)))

object JenaTurtleStringParser
  extends n3.TurtleParser(
    Jena.diesel,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[Jena]](4)))

object JenaTurtleSeqParser
  extends n3.TurtleParser(
    Jena.diesel,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[Jena]](4)))
