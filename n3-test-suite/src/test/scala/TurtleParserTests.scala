/*
 * Copyright (c) 2012 Henry Story
 * under the Open Source MIT Licence http://www.opensource.org/licenses/MIT
 */

package org.w3.rdf.n3

import _root_.nomo.{Accumulators, Errors, Monotypic, Parsers}
import nomo.TurtleReader
import org.w3.rdf.{n3, sesame, jena}
import sesame.SesameGraphIsomorphism
import org.w3.rdf.simple._
import org.w3.rdf.util.DefaultGraphIsomorphism

//class JenaTurtleParserStringTest extends n3.TurtleParserTest(,jena.JenaTurtleReader) {
//  val morpheus = jena.JenaGraphIsomorphism
//}
//
//class JenaTurtleParserSeqTest extends n3.TurtleParserTest(jena.JenaTurtleSeqParser,jena.JenaTurtleReader) {
//  val morpheus = jena.JenaGraphIsomorphism
//}
//

//this is kind of useful enough to be in the n3 lib
object NomoTurtleSeqReader extends TurtleReader(TurtleSeqParser)

object TurtleSeqParser
  extends TurtleParser(
    SimpleRDFOperations,
    Parsers(
      Monotypic.Seq[Char],
      Errors.tree[Char],
      Accumulators.position[Listener[SimpleRDF]](4)))

object SimpleGraphIsomorphism extends DefaultGraphIsomorphism(SimpleRDFOperations)

/**
 * Test the nomo parser with the sesame Turtle Reader
 */
class NomoOnSesameParserSeqTest extends n3.TurtleParserTest(
    NomoTurtleSeqReader,
    sesame.SesameTurtleReader,
    SimpleRDFOperations,
    sesame.SesameOperations) {
  val morpheus = SesameGraphIsomorphism
}

/**
 * Test Sesame's Parser with the Sesame Parser
 */
class SesameOnSesameParserTest extends n3.TurtleParserTest(
    sesame.SesameTurtleReader,
    sesame.SesameTurtleReader,
    sesame.SesameOperations,
    sesame.SesameOperations) {
  val morpheus = SesameGraphIsomorphism //todo: replace with identityIsomorphism
}

/**
 * Test Jena's Turtle Parser with Jena's Parser
 */
class JenaOnJenaParserTest extends n3.TurtleParserTest(
    jena.JenaTurtleReader,
    jena.JenaTurtleReader,
    jena.JenaOperations,
    jena.JenaOperations) {
  val morpheus = jena.JenaGraphIsomorphism //todo: replace with identityIsomorphism
}


object SimpleNTriplesParserSpec extends n3.NTriplesSpec(SimpleRDFOperations)

object SimpleTurtleParserSpec extends n3.TurtleSpec(SimpleRDFOperations, SimpleGraphIsomorphism)

//class NTriplesParserStringTest extends n3.NTriplesParserTest(SimpleRDFOperations, SimpleNTriplesStringParser) {
//  val isomorphism = SimpleGraphIsomorphism
//  def toF(string: String) = string
//}
//
//
//class NTriplesParserSeqTest extends n3.NTriplesParserTest(SimpleRDFOperations, SimpleNTriplesSeqParser) {
//  val isomorphism = SimpleGraphIsomorphism
//  def toF(string: String) = string.toSeq
//}
