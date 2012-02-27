package org.w3.rdf.simple

import org.w3.rdf._
import org.w3.rdf.util.DefaultGraphIsomorphism

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._

class SimpleModuleTest extends PimpsTestSuite(SimpleRDFOperations)

object SimpleNTriplesParserSpec extends n3.NTriplesSpec(SimpleRDFOperations)

object SimpleTurtleParserSpec extends n3.TurtleSpec(SimpleRDFOperations, SimpleGraphIsomorphism)

class NTriplesParserStringTest extends n3.NTriplesParserTest(SimpleRDFOperations, SimpleNTriplesStringParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string
}


class NTriplesParserSeqTest extends n3.NTriplesParserTest(SimpleRDFOperations, SimpleNTriplesSeqParser) {
  val isomorphism = SimpleGraphIsomorphism
  def toF(string: String) = string.toSeq
}


object SimpleNTriplesStringParser
  extends n3.NTriplesParser(
    SimpleRDFOperations,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleRDF]](4)))

object SimpleNTriplesSeqParser
  extends n3.NTriplesParser(
    SimpleRDFOperations,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleRDF]](4)))

// TODO come back here when a writer is available
//class SimpleTurtleTest extends TurtleTestSuite(SimpleModule) {
//  val reader = SimpleTurtleReader
//  val iso = SimpleGraphIsomorphism
//}