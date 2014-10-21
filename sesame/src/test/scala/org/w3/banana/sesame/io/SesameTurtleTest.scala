package org.w3.banana.sesame.io

import org.w3.banana.io.{ RdfXMLTestSuite, TurtleTestSuite }
import org.w3.banana.sesame.Sesame
import org.w3.banana.sesame.Sesame._

class SesameTurtleTest extends TurtleTestSuite[Sesame]

class SesameRdfXMLTest extends RdfXMLTestSuite[Sesame]
