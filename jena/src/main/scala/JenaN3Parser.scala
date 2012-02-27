package org.w3.rdf.jena

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

object JenaNTriplesStringParser
extends n3.NTriplesParser(
  JenaOperations,
  Parsers(
    Monotypic.String,
    Errors.tree[Char],
    Accumulators.position[Listener[Jena]](4)))

object JenaNTriplesSeqParser
extends n3.NTriplesParser(
  JenaOperations,
  Parsers(
    Monotypic.Seq[Char],
    Errors.tree[Char],
    Accumulators.position[Listener[Jena]](4)))

object JenaTurtleStringParser
  extends n3.TurtleParser(
    JenaOperations,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[Jena]](4)))

object JenaTurtleSeqParser
  extends n3.TurtleParser(
    JenaOperations,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[Jena]](4)))