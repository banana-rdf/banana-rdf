package org.w3.banana.sesame.io

import org.w3.banana.io._
import org.w3.banana.sesame._
import org.w3.banana.util.tryInstances._

import scala.util.Try

class SesameTurtleTests extends TurtleTestSuite[Sesame, Try]

class SesamePrefixTest extends PrefixTestSuite[Sesame, Try]

class SesameNTripleReaderTestSuite extends NTriplesReaderTestSuite[Sesame]

class SesameNTripleWriterTestSuite extends NTriplesWriterTestSuite[Sesame]

class SesameRdfXMLTests extends RdfXMLTestSuite[Sesame, Try]

class SesameJsonLDCompactedTests extends JsonLDTestSuite[Sesame, Try, JsonLdCompacted]

class SesameJsonLDExpandedTests extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]

class SesameJsonLDFlattened extends JsonLDTestSuite[Sesame, Try, JsonLdExpanded]
