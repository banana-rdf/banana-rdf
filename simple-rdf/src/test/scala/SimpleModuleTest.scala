package org.w3.rdf.simple

import org.w3.rdf._
import org.w3.rdf.util.DefaultGraphIsomorphism

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

class SimpleModuleTest extends PimpsTestSuite(SimpleModule)

object SimpleN3ParserSpec extends n3.NTriplesSpec(SimpleModule)

object SimpleTurtleParserSpec extends n3.TurtleSpec(SimpleModule)

class NTriplesParserStringTest extends n3.ParserTest(SimpleModule, SimpleNTriplesStringParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string
}


class NTriplesParserSeqTest extends n3.ParserTest(SimpleModule, SimpleNTriplesSeqParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string.toSeq
}


object SimpleNTriplesStringParser
  extends n3.NTriplesParser(
    SimpleModule,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleModule.type]](4)))

object SimpleNTriplesSeqParser
  extends n3.NTriplesParser(
    SimpleModule,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleModule.type]](4)))

// TODO come back here when a writer is available
//class SimpleTurtleTest extends TurtleTestSuite(SimpleModule) {
//  val reader = SimpleTurtleReader
//  val iso = SimpleGraphIsomorphism
//}