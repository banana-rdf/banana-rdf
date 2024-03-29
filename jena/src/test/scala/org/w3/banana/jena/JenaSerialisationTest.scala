package org.w3.banana.jena

import org.w3.banana.io._
import org.w3.banana.util.tryInstances._

import scala.util.Try

class JenaTurtleTest extends TurtleTestSuite[Jena, Try]

class JenaRelativeTurtleTest extends RelativeTurtleTestSuite[Jena, Try]

class JenaPrefixTest extends PrefixTestSuite[Jena, Try]

class JenaNTripleReaderTestSuite extends NTriplesReaderTestSuite[Jena]

class JenaRdfXMLTest extends RdfXMLTestSuite[Jena, Try]

