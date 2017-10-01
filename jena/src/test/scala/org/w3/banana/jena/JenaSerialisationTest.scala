package org.w3.banana.jena

import org.w3.banana.io._

import scala.util.Try
import org.w3.banana.util.tryInstances._

class JenaTurtleTest extends TurtleTestSuite[Jena, Try]

class JenaTriGTest extends TriGTestSuite[Jena, Try]

class JenaTriGReaderTestSuite extends TriGReaderTestSuite[Jena]

class JenaNTripleReaderTestSuite extends NTriplesReaderTestSuite[Jena]

class JenaRdfXMLTest extends RdfXMLTestSuite[Jena, Try]
