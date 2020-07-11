package org.w3.banana.rdf4j.io

import org.w3.banana.io._
import org.w3.banana.rdf4j.Rdf4j
import org.w3.banana.util.tryInstances._

import scala.util.Try

class Rdf4jTurtleTests extends TurtleTestSuite[Rdf4j, Try]

class Rdf4jPrefixTest extends PrefixTestSuite[Rdf4j, Try]

class Rdf4jNTripleReaderTestSuite extends NTriplesReaderTestSuite[Rdf4j]

class Rdf4jNTripleWriterTestSuite extends NTriplesWriterTestSuite[Rdf4j]

class Rdf4jRdfXMLTests extends RdfXMLTestSuite[Rdf4j, Try]

class Rdf4jJsonLDCompactedTests extends JsonLDTestSuite[Rdf4j, Try, JsonLdCompacted]

class Rdf4jJsonLDExpandedTests extends JsonLDTestSuite[Rdf4j, Try, JsonLdExpanded]

class Rdf4jJsonLDFlattened extends JsonLDTestSuite[Rdf4j, Try, JsonLdExpanded]
