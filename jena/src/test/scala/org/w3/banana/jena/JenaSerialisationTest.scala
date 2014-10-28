package org.w3.banana.jena

import org.w3.banana.io.{ RdfXMLTestSuite, TurtleTestSuite }

import scala.util.Try
import org.w3.banana.util.tryInstances._

class JenaTurtleTest extends TurtleTestSuite[Jena, Try]

class JenaRdfXMLTest extends RdfXMLTestSuite[Jena, Try]
