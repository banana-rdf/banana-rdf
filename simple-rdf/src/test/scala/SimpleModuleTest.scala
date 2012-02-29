package org.w3.rdf.simple

import org.w3.rdf._
import org.w3.rdf.util.DefaultGraphIsomorphism

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._
import nomo.NTriplesReader

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

object TurtleStringParser
  extends TurtleParser(
    SimpleRDFOperations,
    Parsers(
      Monotypic.String,
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleRDF]](4)))

object TurtleSeqParser
  extends TurtleParser(
    SimpleRDFOperations,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleRDF]](4)))

class NomoTurtleParserSeqTest_1 extends n3.TurtleParserTest(TurtleSeqParser,simple.NTriplesSeqReader) {
  info("Official W3C Test of the Nomo Turtle Seq[Char] parser with the Nomo NTriples Sequence Reader")

  val morpheus = SimpleGraphIsomorphism
}

class NomoTurtleParserSeqTest_2 extends n3.TurtleParserTest(TurtleSeqParser,TurtleSeqReader) {
  info("Official W3C Test of Test the Nomo Turtle Sequence parser with itself as the NTriples Reader")
  val morpheus = SimpleGraphIsomorphism
}


// TODO come back here when a writer is available
//class SimpleTurtleTest extends TurtleTestSuite(SimpleModule) {
//  val reader = SimpleTurtleReader
//  val iso = SimpleGraphIsomorphism
//}