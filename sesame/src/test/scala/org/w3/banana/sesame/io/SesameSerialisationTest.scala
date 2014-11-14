package org.w3.banana.sesame.io

import org.w3.banana.io.{NTriplesTestSuite, RdfXMLTestSuite, TurtleTestSuite}
import org.w3.banana.sesame._
import org.w3.banana.util.tryInstances._

import scala.util.Try


class SesameTurtleTest extends TurtleTestSuite[Sesame, Try]

class SesameNTripleTestSuite extends NTriplesTestSuite[Sesame]

class SesameRdfXMLTest extends RdfXMLTestSuite[Sesame, Try]
