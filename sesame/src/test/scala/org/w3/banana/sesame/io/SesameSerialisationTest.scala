package org.w3.banana.sesame.io

import org.w3.banana.io.{NTriplesReaderTestSuite, RdfXMLTestSuite, TurtleTestSuite}
import org.w3.banana.sesame._
import org.w3.banana.util.tryInstances._

import scala.util.Try


class SesameTurtleTest extends TurtleTestSuite[Sesame, Try]

class SesameNTripleReaderTestSuite extends NTriplesReaderTestSuite[Sesame]

class SesameRdfXMLTest extends RdfXMLTestSuite[Sesame, Try]
