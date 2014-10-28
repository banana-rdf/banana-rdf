package org.w3.banana.sesame.io

import org.w3.banana.io.{ RdfXMLTestSuite, TurtleTestSuite }
import org.w3.banana.sesame._

import scala.util.Try
import org.w3.banana.util.tryInstances._

class SesameTurtleTest extends TurtleTestSuite[Sesame, Try]

class SesameRdfXMLTest extends RdfXMLTestSuite[Sesame, Try]
