package org.w3.rdf.simple

import org.w3.rdf._
import org.w3.rdf.jena.util._

import nomo.{Accumulators, Errors, Monotypic, Parsers}
import org.w3.rdf._
import org.w3.rdf.n3._
import nomo.NTriplesReader

class SimpleModuleTest extends PimpsTestSuite(SimpleRDFOperations)

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




// TODO come back here when a writer is available
//class SimpleTurtleTest extends TurtleTestSuite(SimpleModule) {
//  val reader = SimpleTurtleReader
//  val iso = SimpleGraphIsomorphism
//}
